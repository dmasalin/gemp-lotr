package com.gempukku.lotro.cards.set5.raider;

import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Race;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractMinion;

/**
 * Set: Battle of Helm's Deep
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 4
 * Type: Minion • Man
 * Strength: 6
 * Vitality: 1
 * Site: 4
 * Game Text: Southron. Ambush (2). While you can spot a Southron, this minion's twilight cost is - 2.
 */
public class Card5_074 extends AbstractMinion {
    public Card5_074() {
        super(4, 6, 1, 4, Race.MAN, Culture.RAIDER, "Southron Marcher");
        addKeyword(Keyword.SOUTHRON);
        addKeyword(Keyword.AMBUSH, 2);
    }

    @Override
    public int getTwilightCostModifier(LotroGame game, PhysicalCard self, PhysicalCard target) {
        if (Filters.canSpot(game, Keyword.SOUTHRON))
            return -2;
        return 0;
    }
}
