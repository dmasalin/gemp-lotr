package com.gempukku.lotro.cards.set18.gondor;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Treachery & Deceit
 * Side: Free
 * Culture: Gondor
 * Twilight Cost: 4
 * Type: Artifact • Support Area
 * Game Text: To play, spot Aragorn, Elendil, or Isildur. Skirmish: Spot a [GONDOR] Man with 3 or more vitality
 * and discard a [GONDOR] card from hand to make that Man strength +2.
 */
public class Card18_041 extends AbstractPermanent {
    public Card18_041() {
        super(Side.FREE_PEOPLE, 4, CardType.ARTIFACT, Culture.GONDOR, "Crown of Gondor", null, true);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.or(Filters.aragorn, Filters.name("Elendil"), Filters.name("Isildur")));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSpot(game, Culture.GONDOR, Race.MAN, Filters.minVitality(3))
                && PlayConditions.canDiscardFromHand(game, playerId, 1, Culture.GONDOR)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndDiscardCardsFromHandEffect(action, playerId, false, 1, Culture.GONDOR));
            action.appendEffect(
                    new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId, 2, Culture.GONDOR, Race.MAN, Filters.minVitality(3)));
            return Collections.singletonList(action);
        }
        return null;
    }
}
