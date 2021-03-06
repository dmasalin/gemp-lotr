package com.gempukku.lotro.cards.set12.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.DiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseOpponentEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 0
 * Type: Event • Maneuver
 * Game Text: Spot a Dwarf to choose a Shadow player who must spot a race. Discard from play all minions of all other
 * races.
 */
public class Card12_001 extends AbstractEvent {
    public Card12_001() {
        super(Side.FREE_PEOPLE, 0, Culture.DWARVEN, "Argument Ready to Hand", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.DWARF);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, final LotroGame game, final PhysicalCard self) {
        final Filter hasRace = new Filter() {
            @Override
            public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                return physicalCard.getBlueprint().getRace() != null;
            }
        };

        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseOpponentEffect(playerId) {
                    @Override
                    protected void opponentChosen(String opponentId) {
                        action.insertEffect(
                                new ChooseActiveCardEffect(self, opponentId, "Choose character with a race you wish to spare", Filters.character, hasRace) {
                                    @Override
                                    protected void cardSelected(LotroGame game, PhysicalCard card) {
                                        action.insertEffect(
                                                new DiscardCardsFromPlayEffect(self.getOwner(), self, hasRace, CardType.MINION, Filters.not(card.getBlueprint().getRace())));
                                    }
                                });
                    }
                });
        return action;
    }
}
