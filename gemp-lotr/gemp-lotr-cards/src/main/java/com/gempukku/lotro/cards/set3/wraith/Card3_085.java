package com.gempukku.lotro.cards.set3.wraith;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.CostToEffectAction;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.PreventableEffect;
import com.gempukku.lotro.logic.timing.Effect;
import com.gempukku.lotro.logic.timing.UnrespondableEffect;

import java.util.Collections;

/**
 * Set: Realms of Elf-lords
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 0
 * Type: Event
 * Game Text: Maneuver: Spot a Nazgul to wound Gandalf twice. The Free Peoples player may discard 2 [GANDALF] cards
 * from hand to prevent this.
 */
public class Card3_085 extends AbstractEvent {
    public Card3_085() {
        super(Side.SHADOW, 0, Culture.WRAITH, "Too Great and Terrible", Phase.MANEUVER);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Race.NAZGUL);
    }

    @Override
    public PlayEventAction getPlayEventCardAction(final String playerId, LotroGame game, PhysicalCard self) {
        final PlayEventAction action = new PlayEventAction(self);
        action.appendEffect(
                new PreventableEffect(action,
                        new UnrespondableEffect() {
                            @Override
                            public String getText(LotroGame game) {
                                return "Wound Gandalf twice";
                            }

                            @Override
                            protected void doPlayEffect(LotroGame game) {
                                action.appendEffect(
                                        new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, Filters.gandalf));
                                action.appendEffect(
                                        new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, Filters.gandalf));
                            }
                        }, Collections.singletonList(game.getGameState().getCurrentPlayerId()),
                        new PreventableEffect.PreventionCost() {
                            @Override
                            public Effect createPreventionCostForPlayer(CostToEffectAction subAction, String playerId) {
                                return new ChooseAndDiscardCardsFromHandEffect(subAction, playerId, false, 2, 2, Culture.GANDALF) {
                                     @Override
                                     public String getText(LotroGame game) {
                                        return "Discard 2 [GANDALF] cards from hand";
                                     }
                                };
                            }
                        }
                ));
        return action;
    }
}
