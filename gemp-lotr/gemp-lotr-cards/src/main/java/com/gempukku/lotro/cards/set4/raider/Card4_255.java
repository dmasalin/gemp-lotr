package com.gempukku.lotro.cards.set4.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.ExertCharactersEffect;
import com.gempukku.lotro.logic.effects.RemoveTwilightEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 1
 * Type: Possession • Hand Weapon
 * Strength: +2
 * Game Text: Bearer must be a Southron. Skirmish: Remove (4) and exert bearer to wound a companion or ally bearer
 * is skirmishing.
 */
public class Card4_255 extends AbstractAttachable {
    public Card4_255() {
        super(Side.SHADOW, CardType.POSSESSION, 1, Culture.RAIDER, PossessionClass.HAND_WEAPON, "Southron Spear");
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Keyword.SOUTHRON;
    }

    @Override
    public int getStrength() {
        return 2;
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.SKIRMISH, self, 4)
                && PlayConditions.canExert(self, game, Filters.hasAttached(self))) {
            ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new RemoveTwilightEffect(4));
            action.appendCost(
                    new ExertCharactersEffect(action, self, self.getAttachedTo()));
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, Filters.inSkirmishAgainst(Filters.hasAttached(self))));
            return Collections.singletonList(action);
        }
        return null;
    }
}
