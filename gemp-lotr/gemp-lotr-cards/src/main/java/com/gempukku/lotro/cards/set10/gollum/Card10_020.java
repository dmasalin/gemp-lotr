package com.gempukku.lotro.cards.set10.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.CancelActivatedEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.timing.results.ActivateCardResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mount Doom
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 1
 * Type: Condition • Support Area
 * Game Text: Any site 9 is a Mountain. Shadow: If the fellowship is at any site 9, play Gollum from your discard pile.
 * Response: If the Free Peoples player uses a maneuver or archery special ability, exert Gollum to cancel its effect.
 */
public class Card10_020 extends AbstractPermanent {
    public Card10_020() {
        super(Side.SHADOW, 1, CardType.CONDITION, Culture.GOLLUM, "Final Strike");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new KeywordModifier(self, Filters.and(CardType.SITE, Filters.siteNumber(9)), Keyword.MOUNTAIN));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SHADOW, self, 0)
                && game.getGameState().getCurrentSiteNumber() == 9
                && PlayConditions.canPlayFromDiscard(playerId, game, Filters.gollum)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendEffect(
                    new ChooseAndPlayCardFromDiscardEffect(playerId, game, Filters.gollum));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayAfterActions(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.activated(game, effectResult, Filters.owner(game.getGameState().getCurrentPlayerId()))) {
            ActivateCardResult activateCardResult = (ActivateCardResult) effectResult;
            if (activateCardResult.getActionTimeword() == Phase.MANEUVER || activateCardResult.getActionTimeword() == Phase.ARCHERY
                    && PlayConditions.canExert(self, game, Filters.gollum)) {
                ActivateCardAction action = new ActivateCardAction(self);
                action.appendCost(
                        new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.gollum));
                action.appendEffect(
                        new CancelActivatedEffect(self, activateCardResult));
                return Collections.singletonList(action);
            }
        }
        return null;
    }
}
