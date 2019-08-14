package com.gempukku.lotro.cards.set20.rohan;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.SubAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.CancelSkirmishEffect;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * 1
 * Rohirrim Helm
 * Rohan	Possession •   Hand Weapon
 * Bearer must be a [Rohan] man.
 * Skirmish: Discard this possession to cancel a skirmish involving bearer. A minion in this skirmish may exert to prevent this.
 */
public class Card20_471 extends AbstractAttachableFPPossession {
    public Card20_471() {
        super(1, 0, 0, Culture.ROHAN, PossessionClass.HELM, "Rohirrim Helm");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Culture.ROHAN, Race.MAN);
    }

    @Override
    public List<? extends Action> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new PreventableEffect(action,
                            new CancelSkirmishEffect(self.getAttachedTo()),
                            GameUtils.getShadowPlayers(game),
                            new PreventableEffect.PreventionCost() {
                                @Override
                                public Effect createPreventionCostForPlayer(SubAction subAction, String playerId) {
                                    return new ChooseAndExertCharactersEffect(subAction, playerId, 1, 1, CardType.MINION, Filters.inSkirmish) {
                                        @Override
                                        public String getText(LotroGame game) {
                                            return "Exert minion in skirmish";
                                        }
                                    };
                                }
                            }
                    ));
            return Collections.singletonList(action);
        }
        return null;
    }
}