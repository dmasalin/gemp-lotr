package com.gempukku.lotro.cards.set12.site;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractShadowsSite;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.ResistanceModifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.condition.OrCondition;

import java.util.Collections;
import java.util.List;

/**
 * Set: Black Rider
 * Twilight Cost: 1
 * Type: Site
 * Game Text: Battleground. Forest. While you can spot 3 minions (or 6 companions), each unbound companion
 * is resistance -2.
 */
public class Card12_188 extends AbstractShadowsSite {
    public Card12_188() {
        super("Hill of Sight", 1, Direction.RIGHT);
        addKeyword(Keyword.BATTLEGROUND);
        addKeyword(Keyword.FOREST);
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
return Collections.singletonList(new ResistanceModifier(self, Filters.unboundCompanion, new OrCondition(new SpotCondition(3, CardType.MINION), new SpotCondition(6, CardType.COMPANION)), -2));
}
}
