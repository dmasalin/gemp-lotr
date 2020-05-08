package com.gempukku.lotro.async;

import com.gempukku.lotro.async.handler.UriRequestHandler;
import com.gempukku.lotro.db.IpBanDAO;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class GempukkuHttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final static long SIX_MONTHS = 1000L * 60L * 60L * 24L * 30L * 6L;
    private static Logger _log = Logger.getLogger(GempukkuHttpRequestHandler.class);
    private static Logger _accesslog = Logger.getLogger("access");
    private Map<String, byte[]> _fileCache = Collections.synchronizedMap(new HashMap<String, byte[]>());

    private Map<Type, Object> _objects;
    private UriRequestHandler _uriRequestHandler;
    private IpBanDAO _ipBanDAO;

    public GempukkuHttpRequestHandler(Map<Type, Object> objects, UriRequestHandler uriRequestHandler) {
        _objects = objects;
        _uriRequestHandler = uriRequestHandler;
        _ipBanDAO = (IpBanDAO) _objects.get(IpBanDAO.class);
    }

    private static class RequestInformation {
        private String uri;
        private String remoteIp;
        private long requestTime;

        private RequestInformation(String uri, String remoteIp, long requestTime) {
            this.uri = uri;
            this.remoteIp = remoteIp;
            this.requestTime = requestTime;
        }

        public void printLog(int statusCode, long finishedTime) {
            _accesslog.debug(remoteIp + "," + statusCode + "," + uri + "," + (finishedTime - requestTime));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) {
        if (HttpUtil.is100ContinueExpected(httpRequest))
            send100Continue(ctx);

        String uri = httpRequest.getUri();

        if (uri.indexOf("?") > -1)
            uri = uri.substring(0, uri.indexOf("?"));

        final RequestInformation requestInformation = new RequestInformation(httpRequest.getUri(),
                ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress(),
                System.currentTimeMillis());

        ResponseSender responseSender = new ResponseSender(ctx, httpRequest);

        try {
            if (isBanned(requestInformation.remoteIp))
                responseSender.writeError(404);
            else
                _uriRequestHandler.handleRequest(uri, httpRequest, _objects, responseSender, requestInformation.remoteIp);
        } catch (HttpProcessingException exp) {
            responseSender.writeError(exp.getStatus());
        } catch (Exception exp) {
            _log.error("Error while processing request", exp);
            responseSender.writeError(500);
        }
    }

    private void sendResponse(ChannelHandlerContext ctx, HttpRequest request, FullHttpResponse response) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.write(response);
        ctx.flush();

        if (!keepAlive) {
            // If keep-alive is off, close the connection once the content is fully written.
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private boolean isBanned(String ipAddress) {
        if (_ipBanDAO.getIpBans().contains(ipAddress))
            return true;
        for (String bannedRange : _ipBanDAO.getIpPrefixBans()) {
            if (ipAddress.startsWith(bannedRange))
                return true;
        }
        return false;
    }

    private Map<String, String> getHeadersForFile(Map<String, String> headers, File file) {
        Map<String, String> fileHeaders = new HashMap<String, String>(headers);

        boolean disableCaching = false;
        boolean cache = false;

        String fileName = file.getName();
        String contentType;
        if (fileName.endsWith(".html")) {
            contentType = "text/html; charset=UTF-8";
        } else if (fileName.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        } else if (fileName.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (fileName.endsWith(".jpg")) {
            cache = true;
            contentType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            cache = true;
            contentType = "image/png";
        } else if (fileName.endsWith(".gif")) {
            cache = true;
            contentType = "image/gif";
        } else if (fileName.endsWith(".wav")) {
            cache = true;
            contentType = "audio/wav";
        } else {
            contentType = "application/octet-stream";
        }

        if (disableCaching) {
            fileHeaders.put(HttpHeaderNames.CACHE_CONTROL.toString(), "no-cache");
            fileHeaders.put(HttpHeaderNames.PRAGMA.toString(), "no-cache");
            fileHeaders.put(HttpHeaderNames.EXPIRES.toString(), String.valueOf(-1));
        } else if (cache) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
            long sixMonthsFromNow = System.currentTimeMillis() + SIX_MONTHS;
            fileHeaders.put(HttpHeaderNames.EXPIRES.toString(), dateFormat.format(new Date(sixMonthsFromNow)));
        }

        fileHeaders.put(CONTENT_TYPE.toString(), contentType);
        return fileHeaders;
    }

    private HttpHeaders convertToHeaders(Map<? extends CharSequence, String> headersMap) {
        HttpHeaders headers = new DefaultHttpHeaders();
        if (headersMap != null) {
            for (Map.Entry<? extends CharSequence, String> headerEntry : headersMap.entrySet()) {
                headers.set(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        return headers;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!(cause instanceof IOException) && !(cause instanceof IllegalArgumentException))
            _log.error("Error while processing request", cause);
        ctx.close();
    }

    private class ResponseSender implements ResponseWriter {
        private ChannelHandlerContext ctx;
        private HttpRequest request;

        public ResponseSender(ChannelHandlerContext ctx, HttpRequest request) {
            this.ctx = ctx;
            this.request = request;
        }

        @Override
        public void writeError(int status) {
            byte[] content = new byte[0];
            // Build the response object.
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(status), Unpooled.wrappedBuffer(content), convertToHeaders(null), EmptyHttpHeaders.INSTANCE);
            sendResponse(ctx, request, response);
        }

        @Override
        public void writeError(int status, Map<String, String> headers) {
            byte[] content = new byte[0];
            // Build the response object.
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(status), Unpooled.wrappedBuffer(content), convertToHeaders(headers), EmptyHttpHeaders.INSTANCE);
            sendResponse(ctx, request, response);
        }

        @Override
        public void writeXmlResponse(Document document) {
            writeXmlResponse(document, null);
        }

        @Override
        public void writeXmlResponse(Document document, Map<? extends CharSequence, String> headers) {
            try {
                String contentType;
                String response1;
                if (document != null) {
                    DOMSource domSource = new DOMSource(document);
                    StringWriter writer = new StringWriter();
                    StreamResult result = new StreamResult(writer);
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer transformer = tf.newTransformer();
                    transformer.transform(domSource, result);

                    response1 = writer.toString();
                    contentType = "application/xml; charset=UTF-8";
                } else {
                    response1 = "OK";
                    contentType = "text/plain";
                }
                HttpHeaders headers1 = convertToHeaders(headers);
                headers1.set(CONTENT_TYPE, contentType);

                // Build the response object.
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(response1.getBytes(CharsetUtil.UTF_8)), headers1, EmptyHttpHeaders.INSTANCE);
                sendResponse(ctx, request, response);
            } catch (Exception exp) {
                byte[] content = new byte[0];
                // Build the response object.
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.wrappedBuffer(content), null, EmptyHttpHeaders.INSTANCE);
                sendResponse(ctx, request, response);
            }
        }

        @Override
        public void writeHtmlResponse(String html) {
            HttpHeaders headers = new DefaultHttpHeaders();
            headers.set(CONTENT_TYPE, "text/html; charset=UTF-8");

            // Build the response object.
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(html.getBytes(CharsetUtil.UTF_8)), headers, EmptyHttpHeaders.INSTANCE);
            sendResponse(ctx, request, response);
        }

        @Override
        public void writeByteResponse(byte[] bytes, Map<? extends CharSequence, String> headers) {
            HttpHeaders headers1 = convertToHeaders(headers);

            // Build the response object.
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes), headers1, EmptyHttpHeaders.INSTANCE);
            sendResponse(ctx, request, response);
        }

        @Override
        public void writeFile(File file, Map<String, String> headers) {
            try {
                String canonicalPath = file.getCanonicalPath();
                byte[] fileBytes = _fileCache.get(canonicalPath);
                if (fileBytes == null) {
                    if (!file.exists() || !file.isFile()) {
                        byte[] content = new byte[0];
                        // Build the response object.
                        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(404), Unpooled.wrappedBuffer(content), convertToHeaders(null), EmptyHttpHeaders.INSTANCE);
                        sendResponse(ctx, request, response);
                        return;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        IOUtils.copyLarge(fis, baos);
                        fileBytes = baos.toByteArray();
                        _fileCache.put(canonicalPath, fileBytes);
                    } finally {
                        IOUtils.closeQuietly(fis);
                    }
                }

                HttpHeaders headers1 = convertToHeaders(getHeadersForFile(headers, file));

                // Build the response object.
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(fileBytes), headers1, EmptyHttpHeaders.INSTANCE);
                sendResponse(ctx, request, response);
            } catch (IOException exp) {
                byte[] content = new byte[0];
                // Build the response object.
                FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.valueOf(500), Unpooled.wrappedBuffer(content), convertToHeaders(null), EmptyHttpHeaders.INSTANCE);
                sendResponse(ctx, request, response);
            }
        }
    }
}
