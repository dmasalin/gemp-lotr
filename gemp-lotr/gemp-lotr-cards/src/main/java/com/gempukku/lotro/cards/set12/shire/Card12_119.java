package com.gempukku.lotro.cards.set12.shire;

import com.gempukku.lotro.logic.cardtype.AbstractCompanion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.SelfExertEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPutCardFromDiscardIntoHandEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 2
 * Type: Companion • Hobbit
 * Strength: 3
 * Vitality: 4
 * Resistance: 8
 * Game Text: Ring-bound. At the start of each skirmish involving Bilbo, you may exert him to take a [SHIRE] skirmish
 * event into hand from your discard pile.
 */
public class Card12_119 extends AbstractCompanion {
    public Card12_119() {
        super(2, 3, 4, 8, Culture.SHIRE, Race.HOBBIT, null, "Bilbo", "Melancholy Hobbit", true);
        addKeyword(Keyword.RING_BOUND);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.SKIRMISH)
                && PlayConditions.canSelfExert(self, game)
                && Filters.inSkirmish.accepts(game, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new SelfExertEffect(action, self));
            action.appendEffect(
                    new ChooseAndPutCardFromDiscardIntoHandEffect(action, playerId, 1, 1, Culture.SHIRE, CardType.EVENT, Keyword.SKIRMISH));
            return Collections.singletonList(action);
        }
        return null;
    }
}
