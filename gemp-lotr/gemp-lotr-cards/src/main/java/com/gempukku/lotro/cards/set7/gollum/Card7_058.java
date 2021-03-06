package com.gempukku.lotro.cards.set7.gollum;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 2
 * Type: Minion
 * Strength: 5
 * Vitality: 4
 * Site: 3
 * Game Text: If you have initiative during the Shadow phase, you may play this minion from your discard pile.
 */
public class Card7_058 extends AbstractMinion {
    public Card7_058() {
        super(2, 5, 4, 3, null, Culture.GOLLUM, "Gollum", "Plotting Deceiver", true);
    }

    @Override
    public List<? extends Action> getPhaseActionsFromDiscard(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.isPhase(game, Phase.SHADOW)
                && PlayConditions.hasInitiative(game, Side.SHADOW)
                && PlayConditions.canPlayFromDiscard(playerId, game, self)) {
            return Collections.singletonList(PlayUtils.getPlayCardAction(game, self, 0, Filters.any, false));
        }
        return null;
    }
}
