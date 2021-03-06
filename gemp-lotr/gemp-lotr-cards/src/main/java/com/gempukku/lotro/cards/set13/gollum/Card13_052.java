package com.gempukku.lotro.cards.set13.gollum;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.GameUtils;
import com.gempukku.lotro.logic.actions.PlayEventAction;
import com.gempukku.lotro.logic.cardtype.AbstractEvent;
import com.gempukku.lotro.logic.effects.ChooseAndWoundCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndPlayCardFromDiscardEffect;
import com.gempukku.lotro.logic.timing.PlayConditions;

/**
 * Set: Bloodlines
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 2
 * Type: Event • Shadow
 * Game Text: If the fellowship is in region 1, spot Gollum to wound a companion. If the fellowship is in region 2, spot
 * Gollum to exert a companion. If the fellowship is in region 3, play a [GOLLUM] minion from your discard pile.
 */
public class Card13_052 extends AbstractEvent {
    public Card13_052() {
        super(Side.SHADOW, 2, Culture.GOLLUM, "Little Snuffler", Phase.SHADOW);
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        int region = GameUtils.getRegion(game);
        return (
                ((region == 1 || region == 2) && PlayConditions.canSpot(game, Filters.gollum))
                        || (region == 3 && PlayConditions.canPlayFromDiscard(self.getOwner(), game, Culture.GOLLUM, CardType.MINION)));
    }

    @Override
    public PlayEventAction getPlayEventCardAction(String playerId, LotroGame game, PhysicalCard self) {
        PlayEventAction action = new PlayEventAction(self);
        int region = GameUtils.getRegion(game);
        if (region == 1)
            action.appendEffect(
                    new ChooseAndWoundCharactersEffect(action, playerId, 1, 1, CardType.COMPANION));
        if (region == 2)
            action.appendEffect(
                    new ChooseAndExertCharactersEffect(action, playerId, 1, 1, CardType.COMPANION));
        if (region == 3)
            action.appendEffect(
                    new ChooseAndPlayCardFromDiscardEffect(playerId, game, Culture.GOLLUM, CardType.MINION));
        return action;
    }
}
