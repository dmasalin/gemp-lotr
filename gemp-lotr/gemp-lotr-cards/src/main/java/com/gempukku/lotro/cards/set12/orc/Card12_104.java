package com.gempukku.lotro.cards.set12.orc;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.RevealTopCardsOfDrawDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.List;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Orc
 * Twilight Cost: 1
 * Type: Event • Skirmish
 * Game Text: Reveal the top 5 cards of your draw deck (or, if the fellowship is at a battleground site, the top 7
 * cards) to make an [ORC] minion strength +1 for each [ORC] card revealed.
 */
public class Card12_104 extends AbstractEvent {
    public Card12_104() {
        super(Side.SHADOW, 1, Culture.ORC, "Taunt", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return ((PlayConditions.location(game, Keyword.BATTLEGROUND) && game.getGameState().getDeck(self.getOwner()).size() >= 7)
                || (!PlayConditions.location(game, Keyword.BATTLEGROUND) && game.getGameState().getDeck(self.getOwner()).size() >= 5));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        int count = PlayConditions.location(game, Keyword.BATTLEGROUND) ? 7 : 5;
        action.appendCost(
                new RevealTopCardsOfDrawDeckEffect(self, playerId, count) {
                    @Override
                    protected void cardsRevealed(List<PhysicalCard> revealedCards) {
                        int bonus = Filters.filter(revealedCards, game, Culture.ORC).size();
                        action.appendEffect(
                                new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId, bonus, Culture.ORC, CardType.MINION));
                    }
                });
        return action;
    }
}
