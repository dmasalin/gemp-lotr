package com.gempukku.lotro.cards.set13.elven;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.choose.ChooseAndAddUntilEOPStrengthBonusEffect;
import com.gempukku.lotro.logic.modifiers.evaluator.Evaluator;

/**
 * Set: Bloodlines
 * Side: Free
 * Culture: Elven
 * Twilight Cost: 1
 * Type: Event • Skirmish
 * Game Text: Make an [ELVEN] companion strength +2 (or +5 if the fellowship has moved more than once this turn).
 */
public class Card13_014 extends AbstractEvent {
    public Card13_014() {
        super(Side.FREE_PEOPLE, 1, Culture.ELVEN, "Final Shot", Phase.SKIRMISH);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new ChooseAndAddUntilEOPStrengthBonusEffect(action, self, playerId,
                        new Evaluator() {
                            @Override
                            public int evaluateExpression(LotroGame game, PhysicalCard cardAffected) {
                                if (game.getGameState().getMoveCount() > 1)
                                    return 5;
                                return 2;
                            }
                        }, Culture.ELVEN, CardType.COMPANION));
        return action;
    }
}
