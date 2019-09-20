package com.gempukku.lotro.cards.set32.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.modifiers.evaluator.CardPhaseLimitEvaluator;
import com.gempukku.lotro.logic.modifiers.evaluator.ConstantEvaluator;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Clouds Burst
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 6
 * Type: Minion • Nazgul
 * Strength: 12
 * Vitality: 3
 * Site: 5
 * Game Text: Fierce. This minion is twilight cost -1 for each Wise character you spot (limit -2).
 * Skirmish: Remove (1) to make another [WRAITH] minion strength +1 (limit +3).
 */
public class Card32_067 extends AbstractMinion {
    public Card32_067() {
        super(6, 12, 3, 5, Race.NAZGUL, Culture.WRAITH, Names.attea, "Revived", true);
        addKeyword(Keyword.FIERCE);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        return -Math.min(2, Filters.countActive(game, Keyword.WISE));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, final LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 1)) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(new RemoveTwilightEffect(1));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose another WRAITH minion", Culture.WRAITH, CardType.MINION, Filters.not(self)) {
                        @Override
                        public void cardSelected(LotroGame game, PhysicalCard card) {
                            action.insertEffect(
                                    new AddUntilEndOfPhaseModifierEffect(
                                            new StrengthModifier(self, Filters.sameCard(card), null,
                                                    new CardPhaseLimitEvaluator(game, self, Phase.SKIRMISH, 3, new ConstantEvaluator(1)))));
                        }
                    });
            return Collections.singletonList(action);

        }
        return null;
    }
}
