package com.gempukku.lotro.cards.set4.site;

import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.logic.cardtype.AbstractSite;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Type: Site
 * Site: 1T
 * Game Text: Each time you play a companion here, you must discard 2 cards from hand.
 */
public class Card4_324 extends AbstractSite {
    public Card4_324() {
        super("Eastemnet Downs", SitesBlock.TWO_TOWERS, 1, 0, Direction.LEFT);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, CardType.COMPANION)
                && game.getGameState().getCurrentSite() == self) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(
                    new ChooseAndDiscardCardsFromHandEffect(action, game.getGameState().getCurrentPlayerId(), true, 2));
            return Collections.singletonList(action);
        }
        return null;
    }
}
