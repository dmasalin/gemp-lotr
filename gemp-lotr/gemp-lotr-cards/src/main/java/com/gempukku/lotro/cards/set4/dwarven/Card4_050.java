package com.gempukku.lotro.cards.set4.dwarven;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.ActivateCardAction;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.effects.AddUntilEndOfPhaseModifierEffect;
import com.gempukku.lotro.logic.effects.ChooseActiveCardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndStackCardsFromHandEffect;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Two Towers
 * Side: Free
 * Culture: Dwarven
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: Tale.Plays to your support area.
 * Skirmish: Stack a Free Peoples card from hand here to make a Dwarf damage +1.
 */
public class Card4_050 extends AbstractPermanent {
    public Card4_050() {
        super(Side.FREE_PEOPLE, 1, CardType.CONDITION, Culture.DWARVEN, "Here Is Good Rock", null, true);
        addKeyword(Keyword.TALE);
    }

    @Override
    public List<? extends ActivateCardAction> getPhaseActionsInPlay(String playerId, LotroGame game, final PhysicalCard self) {
        if (PlayConditions.canUseFPCardDuringPhase(game, Phase.SKIRMISH, self)
                && Filters.filter(game.getGameState().getHand(playerId), game, Side.FREE_PEOPLE).size() > 0) {
            final ActivateCardAction action = new ActivateCardAction(self);
            action.appendCost(
                    new ChooseAndStackCardsFromHandEffect(action, playerId, 1, 1, self, Side.FREE_PEOPLE));
            action.appendEffect(
                    new ChooseActiveCardEffect(self, playerId, "Choose a Dwarf", Race.DWARF) {
                        @Override
                        protected void cardSelected(LotroGame game, PhysicalCard card) {
                            action.insertEffect(
                                    new AddUntilEndOfPhaseModifierEffect(
                                            new KeywordModifier(self, Filters.sameCard(card), Keyword.DAMAGE)));
                        }
                    });
            return Collections.singletonList(action);
        }
        return null;
    }
}
