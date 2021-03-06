package com.gempukku.lotro.cards.set8.raider;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.cardtype.AbstractPermanent;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.modifiers.SpotCondition;
import com.gempukku.lotro.logic.modifiers.condition.AndCondition;
import com.gempukku.lotro.logic.modifiers.condition.NotCondition;
import com.gempukku.lotro.logic.modifiers.condition.PhaseCondition;

import java.util.Collections;
import java.util.List;

/**
 * Set: Siege of Gondor
 * Side: Shadow
 * Culture: Raider
 * Twilight Cost: 3
 * Type: Condition • Support Area
 * Game Text: While you can spot a mounted [RAIDER] Man, [RAIDER] Men cannot take wounds (except during skirmishes).
 */
public class Card8_063 extends AbstractPermanent {
    public Card8_063() {
        super(Side.SHADOW, 3, CardType.CONDITION, Culture.RAIDER, "Line of Defense");
    }

    @Override
    public List<? extends Modifier> getInPlayModifiers(LotroGame game, PhysicalCard self) {
        return Collections.singletonList(
                new CantTakeWoundsModifier(self,
                        new AndCondition(
                                new SpotCondition(Culture.RAIDER, Race.MAN, Filters.mounted),
                                new NotCondition(new PhaseCondition(Phase.SKIRMISH))
                        ), Filters.and(Culture.RAIDER, Race.MAN)));
    }
}
