/**
 * The slide-open details drawer shared by the Events tab: the completed-events browser (EventHistoryUI), the Current
 * Leagues / Current Tournaments lists (LeagueResultsUI / TournamentResultsUI in list mode) and the calendar's event
 * preview (EventCalendarUI with leagueUI / tournamentUI).
 *
 * A drawer is an inset pane that hangs under a header element (a table row, or an EventDrawer.row header) and slides
 * open and shut.  Its content is fetched on the first opening; later toggles only slide it.  A failed fetch is
 * retried on the next opening.
 *
 *   var drawer = new EventDrawer({
 *       header: $row,                 // gets class "expanded" while open
 *       button: $button,              // optional jQuery UI button: label and aria-expanded follow the state
 *       closedLabel: "See details", openLabel: "Hide details",
 *       mount: function (slide) {...},// optional: puts the slide wrapper in the DOM and returns the element to hide
 *                                     // while closed (default: inserted right after the header, and hidden itself)
 *       load: function (drawer, content) {
 *           fetch(..., function (data) {
 *               drawer.settle(content, function (c) { c.empty().append(...); });   // return false = failed
 *           }, EventDrawer.errorMap(function (message, status) { drawer.fail(content, message); }));
 *       }
 *   });
 *   drawer.toggle();  drawer.open();  drawer.close();  drawer.refresh();   // open(true) / close(true): no animation
 *
 * EventDrawer.row(...) builds the Events-tab header row ([button] title ...... date) with its drawer underneath.
 *
 * Heights are driven explicitly (EventDrawer.slide / EventDrawer.resize) rather than with slideDown / slideUp, so that
 * reversing mid-animation continues from wherever the pane is, and fetched content that replaces the loading line
 * eases to its new height.  prefers-reduced-motion turns the animation off.
 */
var EventDrawer = Class.extend({
    options: null,
    header: null,
    button: null,
    holder: null,      // what mount() put in the DOM around the slide wrapper; hidden while closed
    slide: null,       // the wrapper whose height animates
    content: null,     // the pane inside it; replaced (so stale responses can be recognised) when an error is retried
    status: null,      // null (never opened) | loading | loaded | error
    expanded: false,

    init: function (options) {
        this.options = $.extend({
            slideClass: "event-drawer-slide",
            paneClass: "event-drawer",
            loadingClass: "event-drawer-loading",
            errorClass: "event-drawer-error",
            loadingText: "Loading details…",
            expandedClass: "expanded",
            closedLabel: "See details",
            openLabel: "Hide details",
            mount: null,
            load: null,
            onToggle: null
        }, options || {});
        this.header = this.options.header || null;
        this.button = this.options.button || null;
    },

    isOpen: function () {
        return this.expanded;
    },

    toggle: function () {
        if (this.expanded)
            this.close();
        else
            this.open();
    },

    // instant: show it fully open straight away (a pane that is born open)
    open: function (instant) {
        if (this.expanded)
            return;
        if (this.holder != null && this.status != "error") {
            this.holder.show();
            this.setExpanded(true);
            this.animate(true, instant);
            return;
        }
        if (this.holder != null)
            this.holder.remove();

        this.content = $("<div></div>").addClass(this.options.paneClass);
        this.slide = $("<div></div>").addClass(this.options.slideClass).append(this.content).hide();
        this.holder = this.mount(this.slide);
        this.setExpanded(true);
        this.startLoad();
        this.animate(true, instant);
    },

    close: function (instant) {
        if (!this.expanded)
            return;
        var that = this;
        this.setExpanded(false);
        var done = function () {
            if (!that.expanded)
                that.holder.hide();
        };
        if (instant) {
            this.slide.stop(true).css({display: "none", height: ""});
            done();
        } else {
            EventDrawer.slide(this.slide, false, done);
        }
    },

    // Fetches and renders the content again in place (e.g. after joining a league), keeping the drawer's open state;
    // the old content stays until the new one replaces it.
    refresh: function () {
        if (this.content == null || this.options.load == null)
            return;
        this.status = "loading";
        this.options.load(this, this.content);
    },

    // Replaces the pane's content through render(content), easing the height if the drawer is open.  Ignored (returns
    // false) when `content` is no longer this drawer's pane, i.e. a late response for a pane since rebuilt.  render()
    // returning false marks the drawer failed, so its next opening fetches again.
    settle: function (content, render) {
        var that = this;
        if (content == null || content !== this.content)
            return false;
        EventDrawer.resize(this.slide, this.expanded, function () {
            that.status = (render(content) === false) ? "error" : "loaded";
        });
        return true;
    },

    fail: function (content, message) {
        var errorClass = this.options.errorClass;
        return this.settle(content, function (c) {
            c.empty().append($("<div></div>").addClass(errorClass).text(message));
            return false;
        });
    },

    // ---- internals ----

    mount: function (slide) {
        if (this.options.mount)
            return this.options.mount(slide) || slide;
        this.header.after(slide);
        return slide;
    },

    startLoad: function () {
        this.status = "loading";
        this.content.empty().append($("<div></div>").addClass(this.options.loadingClass).text(this.options.loadingText));
        if (this.options.load)
            this.options.load(this, this.content);
    },

    animate: function (open, instant) {
        if (instant)
            this.slide.stop(true).css({display: open ? "block" : "none", height: ""});
        else
            EventDrawer.slide(this.slide, open);
    },

    setExpanded: function (expanded) {
        this.expanded = expanded;
        if (this.button != null) {
            this.button.attr("aria-expanded", expanded ? "true" : "false");
            this.button.button("option", "label", expanded ? this.options.openLabel : this.options.closedLabel);
        }
        if (this.header != null)
            this.header.toggleClass(this.options.expandedClass, expanded);
        if (this.options.onToggle)
            this.options.onToggle(expanded);
    }
});

EventDrawer.SLIDE_MS = 250;

EventDrawer.duration = function () {
    if (typeof window != "undefined" && window.matchMedia && window.matchMedia("(prefers-reduced-motion: reduce)").matches)
        return 0;
    return EventDrawer.SLIDE_MS;
};

// Opens (to its natural height) or closes (to nothing) a slide wrapper.  Heights are driven explicitly rather than
// with slideDown / slideUp so that reversing mid-animation starts from wherever the wrapper currently is instead of
// snapping.  `done` runs when the animation completes (not if it is interrupted by another slide / resize).
EventDrawer.slide = function (wrapper, open, done) {
    var from = wrapper.is(":visible") ? wrapper.height() : 0;
    wrapper.stop(true);
    var to = 0;
    if (open) {
        wrapper.css({display: "block", height: "auto"});
        to = wrapper.height();
    }
    wrapper.css({display: "block", height: from + "px"}).animate({height: to + "px"}, EventDrawer.duration(), function () {
        if (open)
            wrapper.css("height", "");
        else
            wrapper.css({display: "none", height: ""});
        if (done)
            done();
    });
};

// Runs `mutate` (which changes the wrapper's content) and, if the wrapper is open, eases from the old height to the
// new one instead of jumping.  A closed wrapper is just updated.
EventDrawer.resize = function (wrapper, open, mutate) {
    if (!open || !wrapper.is(":visible")) {
        mutate();
        return;
    }
    var from = wrapper.height();
    wrapper.stop(true);
    mutate();
    wrapper.css({display: "block", height: "auto"});
    var to = wrapper.height();
    wrapper.css("height", from + "px").animate({height: to + "px"}, EventDrawer.duration(), function () {
        wrapper.css("height", "");
    });
};

// An errorMap for communication.js covering status 0 and 400-599, so no failure reaches the global failure popup;
// report(message, status) shows it inline instead.  A 400 carrying a JSON {"error": "..."} body reports that text.
EventDrawer.errorMap = function (report) {
    var messages = {
        "0": "The server is unreachable or the connection dropped.",
        "401": "You are not logged in.",
        "403": "You do not have permission to see this.",
        "404": "It was not found on the server.",
        "410": "You were logged out after being inactive; refresh the page.",
        "500": "The server failed to produce it."
    };
    var handler = function (xhr) {
        var status = (xhr && xhr.status != null) ? xhr.status : 0;
        var message = messages[String(status)];
        if (status == 400) {
            var body = xhr.responseJSON;
            if (body == null && xhr.responseText) {
                try { body = JSON.parse(xhr.responseText); } catch (ignored) { body = null; }
            }
            var detail = (body && body.error != null) ? body.error
                : (xhr.getResponseHeader ? xhr.getResponseHeader("message") : null);
            message = detail != null ? ("The request was rejected: " + detail) : "The request was rejected as malformed.";
        }
        report(message || ("The server answered with HTTP " + status + "."), status);
    };
    var map = {"0": handler};
    for (var code = 400; code < 600; code++)
        map[String(code)] = handler;
    return map;
};

EventDrawer.selectText = function (node) {
    if (typeof window == "undefined" || !window.getSelection || !document.createRange)
        return;
    var range = document.createRange();
    range.selectNodeContents(node);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
};

// The viewer's user type letters ("a" admin, "l" league admin, ...), or "" while hall.userInfo is not known yet.
EventDrawer.userType = function () {
    var hall = (typeof window != "undefined") ? window.hall : null;
    var type = (hall && hall.userInfo) ? hall.userInfo.type : null;
    return type == null ? "" : String(type);
};

EventDrawer.scrollIntoView = function (element) {
    var node = element && element[0];
    if (node == null || typeof node.scrollIntoView != "function")
        return;
    var smooth = EventDrawer.duration() > 0;
    try {
        node.scrollIntoView({block: "start", behavior: smooth ? "smooth" : "auto"});
    } catch (e) {
        node.scrollIntoView(true);
    }
};

/**
 * One Events-tab header row with its drawer underneath:
 *
 *   [button]  Title  extras ........................................ date
 *   +--------------------------------------------------------------+
 *   |  drawer                                                       |
 *
 *   EventDrawer.row({
 *       kind: "league" | "tournament" | "projected",   // adds class event-row-<kind>
 *       title: "...", date: "...", dateTitle: "tooltip for the date",
 *       extras: [$(...)],          // shown after the title (admin-only bits etc.); caller escapes their content
 *       action: undefined          // the See details / Hide details toggle
 *             | {label, click}     // a fixed button instead (the calendar's "Go to League"); the drawer never closes
 *             | false,             // no button at all
 *       open: true,                // born open (no animation)
 *       load: function (drawer, content) {...},       // as for new EventDrawer
 *       drawer: {...}              // any other EventDrawer options
 *   }) -> {element, header, button, drawer}
 *
 * The header carries class "expanded" while the drawer is open.  Title, date and tooltip go in through .text()/.attr().
 */
EventDrawer.row = function (options) {
    options = options || {};
    var element = $("<div class='event-row'></div>");
    if (options.kind)
        element.addClass("event-row-" + options.kind);
    var header = $("<div class='event-row-header'></div>");
    element.append(header);

    var drawerOptions = $.extend({}, options.drawer || {});
    var button = null;
    if (options.action !== false) {
        button = $("<button type='button' class='event-row-button'></button>");
        header.append(button);
    }
    header.append($("<span class='event-row-title'></span>").text(options.title == null ? "" : String(options.title)));
    if (options.extras && options.extras.length > 0) {
        var extras = $("<span class='event-row-extras'></span>");
        for (var i = 0; i < options.extras.length; i++)
            if (options.extras[i] != null)
                extras.append(options.extras[i]);
        header.append(extras);
    }
    var date = $("<span class='event-row-date'></span>").text(options.date == null ? "" : String(options.date));
    if (options.dateTitle)
        date.attr("title", String(options.dateTitle));
    header.append(date);

    var toggles = options.action == null;
    var drawer = new EventDrawer($.extend(drawerOptions, {
        header: header,
        button: toggles ? button : null,
        load: options.load,
        mount: function (slide) {
            element.append(slide);
            return slide;
        }
    }));

    if (toggles) {
        button.text(drawer.options.closedLabel).attr("aria-expanded", "false").button()
            .click(function () { drawer.toggle(); });
    } else if (button != null) {
        button.addClass("event-row-go").text(String(options.action.label)).button()
            .click(function () { options.action.click(); });
    }
    if (options.open)
        drawer.open(true);
    return {element: element, header: header, button: button, drawer: drawer};
};
