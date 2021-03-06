package com.gempukku.lotro.cards.set3.gandalf;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.modifiers.CantBeAssignedToSkirmishModifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;

/**
 * Set: Realms of Elf-lords
 * Side: Free
 * Culture: Gandalf
 * Twilight Cost: 1
 * Type: Event
 * Game Text: Stealth. Maneuver: At sites 1 to 5, spot Gandalf to prevent Hobbits from being assigned to skirmishes
 * until the regroup phase. At any other site, spot Gandalf to make a Hobbit strength +3 until the regroup phase.
 */
public class Card3_031 extends AbstractEvent {
    public Card3_031() {
        super(Side.FREE_PEOPLE, 1, Culture.GANDALF, "Depart Silently", Phase.MANEUVER);
        addKeyword(Keyword.STEALTH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Filters.gandalf);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, final PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        if (game.getGameState().getCurrentSiteNumber() <= 5 && game.getGameState().getCurrentSiteBlock() == SitesBlock.FELLOWSHIP) {
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new CantBeAssignedToSkirmishModifier(self, Race.HOBBIT), Phase.REGROUP));
        } else {
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a Hobbit", Race.HOBBIT) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.insertEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new StrengthModifier(self, Filters.sameCard(card), 3), Phase.REGROUP));
                        }
                    });
        }
        return action;
    }
}
