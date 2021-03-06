package com.gempukku.lotro.cards.set12.sauron;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAssignCharacterToMinionEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Side: Shadow
 * Culture: Sauron
 * Twilight Cost: 3
 * Type: Minion • Man
 * Strength: 9
 * Vitality: 3
 * Site: 5
 * Game Text: Assignment: Assign The Mouth of Sauron to the companion who has the highest strength. (If two or more are
 * tied for highest, choose one.)
 */
public class Card12_118 extends AbstractMinion {
    public Card12_118() {
        super(3, 9, 3, 5, Race.MAN, Culture.SAURON, "The Mouth of Sauron", "Lieutenant of Barad-dûr", true);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.canUseShadowCardDuringPhase(game, Phase.ASSIGNMENT, self, 0)) {
            ActivateCardAction action = new ActivateCardAction(self);
            int strength = 0;
            for (PhysicalCard companion : Filters.filterActive(game, CardType.COMPANION))
                strength = Math.max(strength, game.getModifiersQuerying().getStrength(game, companion));

            final int highestStrength = strength;

            action.appendEffect(
                    new ChooseAndAssignCharacterToMinionEffect(action, playerId, self, CardType.COMPANION,
                            new Filter() {
                                @Override
                                public boolean accepts(LotroGame game, PhysicalCard physicalCard) {
                                    return game.getModifiersQuerying().getStrength(game, physicalCard) == highestStrength;
                                }
                            }));
            return Collections.singletonList(action);
        }
        return null;
    }
}
