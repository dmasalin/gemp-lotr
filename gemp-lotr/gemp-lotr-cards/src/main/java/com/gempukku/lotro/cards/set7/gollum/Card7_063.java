package com.gempukku.lotro.cards.set7.gollum;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.PlayUtils;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.KeywordModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.PlayConditions;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Return of the King
 * Side: Shadow
 * Culture: Gollum
 * Twilight Cost: 1
 * Type: Condition
 * Game Text: To play, spot Gollum or Smeagol. Bearer must be a minion. Limit 1 per bearer. Bearer is damage +1. If you
 * have initiative during the Shadow phase, you may play this condition from your discard pile.
 */
public class Card7_063 extends AbstractAttachable {
    public Card7_063() {
        super(Side.SHADOW, CardType.CONDITION, 1, Culture.GOLLUM, null, "Let Her Deal With Them");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return PlayConditions.canSpot(game, Filters.gollumOrSmeagol);
    }

    @Override
    public Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.and(CardType.MINION, Filters.not(Filters.hasAttached(Filters.name(getTitle()))));
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new KeywordModifier(self, Filters.hasAttached(self), Keyword.DAMAGE, 1));
    }

    @Override
    public List<? extends Action> getPhaseActionsFromDiscard(String playerId, LotroGame game, PhysicalCard self) {
        if (PlayConditions.isPhase(game, Phase.SHADOW)
                && PlayConditions.hasInitiative(game, Side.SHADOW)
                && PlayUtils.checkPlayRequirements(game, self, Filters.any, 0, 0, false, false)) {
            return Collections.singletonList(PlayUtils.getPlayCardAction(game, self, 0, Filters.any, false));
        }
        return null;
    }
}
