package com.gempukku.lotro.cards.set4.rohan;

import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.results.PlayCardResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Rohan
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: Plays to your support area. Each time your opponent plays a roaming minion, you may spot a villager
 * to exert that minion.
 */
public class Card4_293 extends AbstractPermanent {
    public Card4_293() {
        super(Side.FREE_PEOPLE, 1, CardType.CONDITION, Culture.ROHAN, "Valleys of the Mark");
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, Filters.roamingMinion)
                && PlayConditions.canSpot(game, Keyword.VILLAGER)) {
            PlayCardResult result = (PlayCardResult) effectResult;
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ExertCharactersEffect(action, self, result.getPlayedCard()));
            return Collections.singletonList(action);
        }
        return null;
    }
}
