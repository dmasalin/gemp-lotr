package com.gempukku.lotro.cards.set20.dwarven;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ReplaceInSkirmishEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * 2
 * Battle Fever
 * Dwarven	Event • Skirmish
 * Exert Gimli to replace an unbound companion in a skirmish. He is strength +2 and damage +1 while in that skirmish.
 */
public class Card20_040 extends AbstractEvent {
    public Card20_040() {
        super(Side.FREE_PEOPLE, 2, Culture.DWARVEN, "Battle Fever", Phase.SKIRMISH);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canExert(self, game, Filters.gimli);
    }

    @Override
    public PlayEventAction getPlayCardAction(String playerId, LotroGame game, final PhysicalCard self, int twilightModifier, boolean ignoreRoamingPenalty) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Filters.gimli) {
                    @Override
                    protected void forEachCardExertedCallback(PhysicalCard gimli) {
                        action.appendEffect(
                                new ReplaceInSkirmishEffect(gimli, Filters.unboundCompanion));
                        action.appendEffect(
                                new AddUntilEndOfPhaseModifierEffect(
                                        new StrengthModifier(self, gimli, new SpotCondition(gimli, Filters.inSkirmish), 2)));
                        action.appendEffect(
                                new AddUntilEndOfPhaseModifierEffect(
                                        new KeywordModifier(self, gimli, new SpotCondition(gimli, Filters.inSkirmish), Keyword.DAMAGE, 1)));
                    }
                });
        return action;
    }
}
