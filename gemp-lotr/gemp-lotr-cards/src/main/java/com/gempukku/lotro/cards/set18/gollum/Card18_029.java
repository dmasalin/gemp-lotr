package com.gempukku.lotro.cards.set18.gollum;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.PreventableCardEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPreventCardEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: Response: If a Free Peoples player's card is about to discard your other [GOLLUM] condition, remove (1)
 * to prevent that.
 */
public class Card18_029 extends AbstractPermanent {
    public Card18_029() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.GOLLUM, "Deceit");
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingDiscardedBy(effect, game, Side.FREE_PEOPLE, Filters.not(self), Filters.owner(self.getOwner()), Culture.GOLLUM, CardType.CONDITION)
                && game.getGameState().getTwilightPool() >= 1) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(1));
            action.appendEffect(
                    new ChooseAndPreventCardEffect(self, (PreventableCardEffect) effect, playerId, "Choose condition to prevent discarding", Filters.not(self), Culture.GOLLUM, CardType.CONDITION));
            return Collections.singletonList(action);
        }
        return null;
    }
}
