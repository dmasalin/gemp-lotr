package com.gempukku.lotro.cards.set13.isengard;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.TransferToShadowEffect;
import com.gempukku.lotro.logic.effects.TransferToSupportEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 3
 * Type: Condition • Support Area
 * Game Text: At the start of the maneuver phase, you may exert a Wizard to make this condition a fierce Wizard minion
 * until the start of the regroup phase that has 10 strength and 1 vitality, and cannot take wounds or bear other cards.
 * This card is still a condition.
 */
public class Card13_079 extends AbstractPermanent {
    public Card13_079() {
        super(Side.SHADOW, 3, CardType.CONDITION, Culture.ISENGARD, "Pallando Deceived", null, true);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.MANEUVER)
                && PlayConditions.canExert(self, game, Race.WIZARD)
                && self.getZone() != Zone.SHADOW_CHARACTERS) {
            final OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Race.WIZARD));
            action.appendEffect(
                    new TransferToShadowEffect(self) {
                        @Override
                        protected void cardTransferredCallback() {
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new VitalityModifier(self, self, 1), Phase.REGROUP));
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new StrengthModifier(self, self, 10), Phase.REGROUP));
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new IsAdditionalCardTypeModifier(self, self, CardType.MINION), Phase.REGROUP));
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new KeywordModifier(self, self, Keyword.FIERCE), Phase.REGROUP));
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new MayNotBearModifier(self, self, Filters.any), Phase.REGROUP));
                            action.appendEffect(
                                    new AddUntilStartOfPhaseModifierEffect(
                                            new CantTakeWoundsModifier(self, self), Phase.REGROUP));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, final PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.REGROUP)
                && self.getZone() == Zone.SHADOW_CHARACTERS) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new UnrespondableEffect() {
                        @Override
                        protected void doPlayEffect(LotroGame game) {
                            for (Map.Entry<Token, Integer> tokenCount : game.getGameState().getTokens(self).entrySet()) {
                                if (tokenCount.getValue() > 0)
                                    game.getGameState().removeTokens(self, tokenCount.getKey(), tokenCount.getValue());
                            }
                        }
                    });
            action.appendEffect(
                    new TransferToSupportEffect(self));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public Race getRace() {
        return Race.WIZARD;
    }
}
