package com.gempukku.lotro.cards.set3.sauron;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.DiscardTopCardFromDeckEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Realms of Elf-lords
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 1
 * Type: Event
 * Game Text: Regroup: Exert a [SAURON] minion to discard a card from the top of the Free Peoples player's draw deck
 * for each of these races you can spot in the fellowship: Dwarf, Elf, Man, and Wizard.
 */
public class Card3_092 extends AbstractEvent {
    public Card3_092() {
        super(Side.SHADOW, 1, Culture.SAURON, "Massing in the East", Phase.REGROUP);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Culture.SAURON, CardType.MINION);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Culture.SAURON, CardType.MINION));
        int cardsCount = 0;
        if (Filters.canSpot(game, CardType.COMPANION, Race.DWARF))
            cardsCount++;
        if (Filters.canSpot(game, CardType.COMPANION, Race.ELF))
            cardsCount++;
        if (Filters.canSpot(game, CardType.COMPANION, Race.MAN))
            cardsCount++;
        if (Filters.canSpot(game, CardType.COMPANION, Race.WIZARD))
            cardsCount++;
        if (cardsCount > 0)
            action.appendEffect(
                    new DiscardTopCardFromDeckEffect(self, game.getGameState().getCurrentPlayerId(), cardsCount, true));

        return action;
    }
}
