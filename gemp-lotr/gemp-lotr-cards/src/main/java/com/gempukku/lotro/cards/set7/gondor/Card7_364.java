package com.gempukku.lotro.cards.set7.gondor;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Signet;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.effects.RemoveThreatsEffect;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.modifiers.MaxThreatCondition;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.ForEachThreatEvaluator;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 4
 * Type: Companion • Man
 * Strength: 7
 * Vitality: 4
 * Resistance: 6
 * Signet: Gandalf
 * Game Text: While you cannot spot 4 threats, Aragorn is strength +1 for each threat. Regroup: Exert Aragorn twice
 * to remove 3 threats.
 */
public class Card7_364 extends AbstractCompanion {
    public Card7_364() {
        super(4, 7, 4, 6, Culture.GONDOR, Race.MAN, Signet.GANDALF, "Aragorn", "Driven by Need", true);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new StrengthModifier(self, self, new MaxThreatCondition(3), new ForEachThreatEvaluator()));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.REGROUP, self)
                && PlayConditions.canSelfExert(self, 2, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new RemoveThreatsEffect(self, 3));
            return Collections.singletonList(action);
        }
        return null;
    }
}
