package com.gempukku.lotro.cards.set4.raider;

import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 4
 * Type: Minion • Man
 * Strength: 9
 * Vitality: 2
 * Site: 4
 * Game Text: Southron. At the start of each skirmish involving this minion, you may remove (3) to wound a companion
 * or ally he is skirmishing.
 */
public class Card4_220 extends AbstractMinion {
    public Card4_220() {
        super(4, 9, 2, 4, Race.MAN, Culture.RAIDER, "Desert Soldier");
        addKeyword(Keyword.SOUTHRON);
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.startOfPhase(game, effectResult, Phase.SKIRMISH)
                && PlayConditions.canSpot(game, self, Filters.inSkirmish)
                && game.getGameState().getTwilightPool() >= 3) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(3));
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, Filters.or(CardType.COMPANION, CardType.ALLY), Filters.inSkirmishAgainst(self)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
