package com.gempukku.lotro.cards.set12.shire;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddUntilStartOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Shire
 * Twilight Cost: 2
 * Type: Condition • Support Area
 * Game Text: To play, spot a Hobbit. Skirmish: If your Ring-bearer is assigned to a skirmish, discard this condition to
 * make each companion who has resistance 7 or more strength +2 until the regroup phase.
 */
public class Card12_124 extends AbstractPermanent {
    public Card12_124() {
        super(Side.FREE_PEOPLE, 2, CardType.CONDITION, Culture.SHIRE, "Long Live the Halflings");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.HOBBIT);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSpot(game, Filters.ringBearer, Filters.assignedToSkirmish)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new AddUntilStartOfPhaseModifierEffect(
                            new StrengthModifier(self, Filters.and(CardType.COMPANION, Filters.minResistance(7)), 2), Phase.REGROUP));
            return Collections.singletonList(action);
        }
        return null;
    }
}
