package com.gempukku.lotro.cards.set4.dwarven;

import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 1
 * Type: Event
 * Game Text: Maneuver: Spot a Dwarf companion and an Elf companion to wound a minion.
 */
public class Card4_053 extends AbstractEvent {
    public Card4_053() {
        super(Side.FREE_PEOPLE, 1, Culture.DWARVEN, "Quick As May Be", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile)
                && Filters.canSpot(game.getGameState(), game.getModifiersQuerying(), Race.DWARF, CardType.COMPANION)
                && Filters.canSpot(game.getGameState(), game.getModifiersQuerying(), Race.ELF, CardType.COMPANION);
    }

    @Override
    public PlayEventAction getPlayCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.MINION));
        return action;
    }
}
