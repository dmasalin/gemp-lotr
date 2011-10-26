package com.gempukku.lotro.game.state;

import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.logic.timing.GameStats;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Map;

public class EventSerializer {
    public Node serializeEvent(Document doc, GameEvent gameEvent) {
        Element eventElem = doc.createElement("ge");
        eventElem.setAttribute("type", gameEvent.getType().name());
        if (gameEvent.getBlueprintId() != null)
            eventElem.setAttribute("blueprintId", gameEvent.getBlueprintId());
        if (gameEvent.getCardId() != null)
            eventElem.setAttribute("cardId", gameEvent.getCardId().toString());
        if (gameEvent.getIndex() != null)
            eventElem.setAttribute("index", gameEvent.getIndex().toString());
        if (gameEvent.getControllerId() != null)
            eventElem.setAttribute("controllerId", gameEvent.getControllerId());
        if (gameEvent.getParticipantId() != null)
            eventElem.setAttribute("participantId", gameEvent.getParticipantId());
        if (gameEvent.getAllParticipantIds() != null) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String participantId : gameEvent.getAllParticipantIds()) {
                if (!first) sb.append(",");
                sb.append(participantId);
                first = false;
            }
            eventElem.setAttribute("allParticipantIds", sb.toString());
        }
        if (gameEvent.getPhase() != null)
            eventElem.setAttribute("phase", gameEvent.getPhase().name());
        if (gameEvent.getTargetCardId() != null)
            eventElem.setAttribute("targetCardId", gameEvent.getTargetCardId().toString());
        if (gameEvent.getZone() != null)
            eventElem.setAttribute("zone", gameEvent.getZone().name());
        if (gameEvent.getToken() != null)
            eventElem.setAttribute("token", gameEvent.getToken().name());
        if (gameEvent.getCount() != null)
            eventElem.setAttribute("count", gameEvent.getCount().toString());
        if (gameEvent.getOtherCardIds() != null) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (int cardId : gameEvent.getOtherCardIds()) {
                if (!first) sb.append(",");
                sb.append(cardId);
                first = false;
            }
            eventElem.setAttribute("otherCardIds", sb.toString());
        }
        if (gameEvent.getMessage() != null)
            eventElem.setAttribute("message", gameEvent.getMessage());
        if (gameEvent.getGameStats() != null) {
            GameStats gameStats = gameEvent.getGameStats();
            eventElem.setAttribute("fellowshipArchery", String.valueOf(gameStats.getFellowshipArchery()));
            eventElem.setAttribute("shadowArchery", String.valueOf(gameStats.getShadowArchery()));

            eventElem.setAttribute("fellowshipStrength", String.valueOf(gameStats.getFellowshipSkirmishStrength()));
            eventElem.setAttribute("shadowStrength", String.valueOf(gameStats.getShadowSkirmishStrength()));

            eventElem.setAttribute("moveLimit", String.valueOf(gameStats.getMoveLimit()));
            eventElem.setAttribute("moveCount", String.valueOf(gameStats.getMoveCount()));

            for (Map.Entry<String, Map<Zone, Integer>> playerZoneSizes : gameStats.getZoneSizes().entrySet()) {
                final Element playerZonesElem = doc.createElement("playerZones");

                playerZonesElem.setAttribute("name", playerZoneSizes.getKey());

                for (Map.Entry<Zone, Integer> zoneSizes : playerZoneSizes.getValue().entrySet())
                    playerZonesElem.setAttribute(zoneSizes.getKey().name(), zoneSizes.getValue().toString());

                eventElem.appendChild(playerZonesElem);
            }

            Map<Integer, Integer> charVitalities = gameStats.getCharVitalities();

            StringBuilder charStr = new StringBuilder();
            for (Map.Entry<Integer, Integer> characters : gameStats.getCharStrengths().entrySet()) {
                charStr.append("," + characters.getKey() + "=" + characters.getValue() + "|" + charVitalities.get(characters.getKey()));
            }
            if (charStr.length() > 0)
                charStr.delete(0, 1);

            if (charStr.length() > 0)
                eventElem.setAttribute("charStats", charStr.toString());
        }

        return eventElem;
    }
}
