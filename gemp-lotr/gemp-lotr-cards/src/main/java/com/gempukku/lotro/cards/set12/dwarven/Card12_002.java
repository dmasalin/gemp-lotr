package com.gempukku.lotro.cards.set12.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachableFPPossession;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndTransferAttachableEffect;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.RemoveKeywordModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 0
 * Type: Possession • Armor
 * Game Text: Bearer must be a Dwarf. Each minion skirmishing bearer loses all damage bonus. Skirmish: Discard this
 * possession to transfer one of bearer's possessions to another eligible bearer.
 */
public class Card12_002 extends AbstractAttachableFPPossession {
    public Card12_002() {
        super(0, 0, 0, Culture.DWARVEN, PossessionClass.ARMOR, "Belt of Erebor");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Race.DWARF;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new RemoveKeywordModifier(self, Filters.inSkirmishAgainst(Filters.hasAttached(self)), Keyword.DAMAGE));
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && PlayConditions.canSelfDiscard(self, game)) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new SelfDiscardEffect(self));
            action.appendEffect(
                    new ChooseAndTransferAttachableEffect(action, playerId, CardType.POSSESSION, self.getAttachedTo(), Filters.any));
            return Collections.singletonList(action);
        }
        return null;
    }
}
