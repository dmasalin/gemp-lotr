package com.gempukku.lotro.tournament;

import com.gempukku.lotro.game.LotroGameParticipant;

public interface TournamentCallback {
    public void createGame(LotroGameParticipant playerOne, LotroGameParticipant playerTwo);

    public void broadcastMessage(String message);
}
