package com.gempukku.lotro.cards.set13.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.actions.OptionalTriggerAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.PreventCardEffect;
import com.gempukku.lotro.logic.effects.PreventableCardEffect;
import com.gempukku.lotro.logic.effects.ReinforceTokenEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndRemoveCultureTokensFromCardEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.EffectResult;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 1
 * Type: Possession • Mount
 * Resistance: +1
 * Game Text: Bearer must be a Dwarf. When you play Arod, you may reinforce a [DWARVEN] token. Response: If bearer is
 * about to take a wound during a skirmish, remove 2 [DWARVEN] tokens to prevent that.
 */
public class Card13_001 extends AbstractAttachableFPPossession {
    public Card13_001() {
        super(1, 0, 0, Culture.DWARVEN, PossessionClass.MOUNT, "Arod", "Rohirrim Steed", true);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Race.DWARF;
    }

    @Override
    public int getResistance() {
        return 1;
    }

    @Override
    public List<OptionalTriggerAction> getOptionalAfterTriggers(String playerId, LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.played(game, effectResult, self)) {
            OptionalTriggerAction action = new OptionalTriggerAction(self);
            action.appendEffect(
                    new ReinforceTokenEffect(self, playerId, Token.DWARVEN));
            return Collections.singletonList(action);
        }
        return null;
    }

    @Override
    public List<? extends ActivateCardAction> getOptionalInPlayBeforeActions(String playerId, LotroGame game, Effect effect, PhysicalCard self) {
        if (TriggerConditions.isGettingWounded(effect, game, self.getAttachedTo())
                && PlayConditions.isPhase(game, Phase.SKIRMISH)
                && PlayConditions.canRemoveTokensFromAnything(game, Token.DWARVEN, 2)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.DWARVEN, 1, Filters.any));
            action.appendCost(
                    new ChooseAndRemoveCultureTokensFromCardEffect(self, playerId, Token.DWARVEN, 1, Filters.any));
            action.appendEffect(
                    new PreventCardEffect((PreventableCardEffect) effect, self.getAttachedTo()));
            return Collections.singletonList(action);
        }
        return null;
    }
}
