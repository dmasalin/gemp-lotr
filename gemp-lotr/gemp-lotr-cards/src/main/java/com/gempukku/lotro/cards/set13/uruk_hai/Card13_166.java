package com.gempukku.lotro.cards.set13.uruk_hai;

import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.effects.discount.RemoveCardsFromDiscardDiscountEffect;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 2
 * Type: Event • Skirmish
 * Game Text: You may remove from the game 4 [URUK-HAI] cards in your discard pile instead of paying the twilight cost
 * for this card. Make your [URUK-HAI] minion skirmishing a companion with resistance 4 or less strength +2.
 */
public class Card13_166 extends AbstractEvent {
    public Card13_166() {
        super(Side.SHADOW, 2, Culture.URUK_HAI, "New Enemy", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, int twilightModifier, boolean ignoreRoamingPenalty, boolean ignoreCheckingDeadPile) {
        if (PlayConditions.canRemoveFromDiscard(self, game, playerId, 4, Culture.URUK_HAI))
            twilightModifier -= 1000;
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, twilightModifier, ignoreRoamingPenalty, ignoreCheckingDeadPile);
    }

    @Override
    public PlayEventAction getPlayCardAction(String playerId, LotroGame game, PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        PlayEventAction action = new PlayEventAction(self);
        action.setDiscountEffect(
                new RemoveCardsFromDiscardDiscountEffect(self, playerId, 4, Culture.URUK_HAI));
        action.appendEffect(
                new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId, 2, Filters.owner(playerId), Culture.URUK_HAI, CardType.MINION, Filters.inSkirmishAgainst(CardType.COMPANION, Filters.maxResistance(4))));
        return action;
    }

}
