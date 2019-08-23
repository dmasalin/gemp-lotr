package com.gempukku.lotro.cards.set17.uruk_hai;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndReturnCardsToHandEffect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Rise of Saruman
 * Side: Shadow
 * Culture: Uruk-hai
 * Twilight Cost: 2
 * Type: Possession • Support Area
 * Game Text: To play, spot an [URUK-HAI] hunter. At the start of each Shadow phase, you may discard 2 [URUK-HAI] cards
 * from hand to play an [URUK-HAI] hunter from your discard pile. Regroup: Discard this possession to return
 * an [URUK-HAI] hunter from play to your hand.
 */
public class Card17_118 extends AbstractPermanent {
    public Card17_118() {
        super(Side.SHADOW, 2, CardType.POSSESSION, Culture.URUK_HAI, "Vile Pit", null, true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Culture.URUK_HAI, Keyword.HUNTER);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.SHADOW)
                && PlayConditions.canDiscardFromHand(game, playerId, 2, Culture.URUK_HAI)
                && PlayConditions.canPlayFromDiscard(playerId, game, Culture.URUK_HAI, Keyword.HUNTER)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 2, Culture.URUK_HAI));
            action.appendEffect(
                    new ChooseAndPlayCardFromDiscardEffect(playerId, game, Culture.URUK_HAI, Keyword.HUNTER));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.REGROUP, self, 0)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseAndReturnCardsToHandEffect(action, playerId, 1, 1, Culture.URUK_HAI, Keyword.HUNTER));
            return Collections.singletonList(action);
        }
        return null;
    }
}
