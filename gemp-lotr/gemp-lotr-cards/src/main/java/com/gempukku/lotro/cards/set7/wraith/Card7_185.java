package com.gempukku.lotro.cards.set7.wraith;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndDiscardCardsFromPlayEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: To play, spot a Nazgul. Skirmish: Remove (3) and spot 3 Nazgul to discard a possession or condition borne
 * by a companion a Nazgul is skirmishing.
 */
public class Card7_185 extends AbstractPermanent {
    public Card7_185() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.WRAITH, "Morgul Answers");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Race.NAZGUL);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 3)
                && PlayConditions.canSpot(game, 3, Race.NAZGUL)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(3));
            action.appendEffect(
                    new ChooseAndDiscardCardsFromPlayEffect(action, playerId, 1, 1, Filters.or(CardType.POSSESSION, CardType.CONDITION), Filters.attachedTo(CardType.COMPANION, Filters.inSkirmishAgainst(Race.NAZGUL))));
            return Collections.singletonList(action);
        }
        return null;
    }
}
