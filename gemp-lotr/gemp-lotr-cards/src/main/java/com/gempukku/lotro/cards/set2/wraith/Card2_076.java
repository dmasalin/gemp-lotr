package com.gempukku.lotro.cards.set2.wraith;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.RemoveGameTextModifier;

import java.util.Collections;
import java.util.List;

/**
 * Set: Mines of Moria
 * Side: Shadow
 * Culture: Wraith
 * Twilight Cost: 0
 * Type: Condition
 * Game Text: To play, spot Frodo and a Nazgul. Plays on Sam. Sam's game text does not apply.
 */
public class Card2_076 extends AbstractAttachable {
    public Card2_076() {
        super(Side.SHADOW, CardType.CONDITION, 0, Culture.WRAITH, null, "Helpless");
    }

    @Override
    public boolean checkPlayRequirements(LotroGame game, PhysicalCard self) {
        return Filters.canSpot(game, Filters.frodo)
                && Filters.canSpot(game, Race.NAZGUL);
    }

    @Override
    public Filter getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return Filters.sam;
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new RemoveGameTextModifier(self, Filters.hasAttached(self)));
    }
}
