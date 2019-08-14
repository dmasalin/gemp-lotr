package com.gempukku.lotro.cards.set4.isengard;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.*;
import com.gempukku.lotro.logic.modifiers.cost.ExertExtraPlayCostModifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 0
 * Type: Condition
 * Strength: -1
 * Game Text: Spell. To play, exert Saruman or an [ISENGARD] Man. Plays on a Free Peoples Man. Special abilities
 * in bearer's game text may not be used.
 */
public class Card4_157 extends AbstractAttachable {
    public Card4_157() {
        super(Side.SHADOW, CardType.CONDITION, 0, Culture.ISENGARD, null, "Leechraft");
        addKeyword(Keyword.SPELL);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Filters.or(Filters.saruman, Filters.and(Culture.ISENGARD, Race.MAN)));
    }

    @Override
    public List<? extends AbstractExtraPlayCostModifier> getExtraCostToPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new ExertExtraPlayCostModifier(self, self, null, 1, Filters.or(Filters.saruman, Filters.and(Culture.ISENGARD, Race.MAN))));
    }

    @Override
    public Filter getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(Side.FREE_PEOPLE, Race.MAN);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, final PhysicalCard self) {
        List<Modifier> modifiers = new LinkedList<Modifier>();
        modifiers.add(
                new StrengthModifier(self, Filters.hasAttached(self), -1));
        modifiers.add(
                new AbstractModifier(self, null, null, ModifierEffect.ACTION_MODIFIER) {
                    @Override
                    public boolean canPlayAction(LotroGame game, String performingPlayer, Action action) {
                        if (action.getActionSource() == self.getAttachedTo())
                            return false;
                        return true;
                    }
                });
        return modifiers;
    }
}
