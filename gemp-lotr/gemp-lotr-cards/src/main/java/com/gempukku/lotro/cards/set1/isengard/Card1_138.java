package com.gempukku.lotro.cards.set1.isengard;

import com.gempukku.lotro.logic.cardtype.AbstractAttachable;
import com.gempukku.lotro.logic.timing.PlayConditions;
import com.gempukku.lotro.logic.timing.TriggerConditions;
import com.gempukku.lotro.logic.actions.AttachPermanentAction;
import com.gempukku.lotro.logic.effects.SelfDiscardEffect;
import com.gempukku.lotro.logic.effects.choose.ChooseAndExertCharactersEffect;
import com.gempukku.lotro.logic.modifiers.PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier;
import com.gempukku.lotro.logic.modifiers.conditions.LocationCondition;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.filters.Filter;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.PhysicalCard;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.modifiers.Modifier;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

/**
 * Set: The Fellowship of the Ring
 * Side: Shadow
 * Culture: Isengard
 * Twilight Cost: 2
 * Type: Condition
 * Game Text: Spell. Weather. To play, exert a [ISENGARD] minion. Plays on a site. No player may play skirmish events
 * or use skirmish special abilities during skirmishes at this site. Discard this condition at the end of the turn.
 */
public class Card1_138 extends AbstractAttachable {
    public Card1_138() {
        super(Side.SHADOW, CardType.CONDITION, 2, Culture.ISENGARD, null, "Saruman's Snows");
        addKeyword(Keyword.SPELL);
        addKeyword(Keyword.WEATHER);
    }

    @Override
    protected Filterable getValidTargetFilter(String playerId, LotroGame game, PhysicalCard self) {
        return CardType.SITE;
    }

    @Override
    public boolean checkPlayRequirements(String playerId, LotroGame game, PhysicalCard self, int withTwilightRemoved, Filter additionalAttachmentFilter, int twilightModifier) {
        return super.checkPlayRequirements(playerId, game, self, withTwilightRemoved, additionalAttachmentFilter, twilightModifier)
                && PlayConditions.canExert(self, game, Culture.ISENGARD, CardType.MINION);
    }

    @Override
    public AttachPermanentAction getPlayCardAction(String playerId, LotroGame game, PhysicalCard self, Filterable additionalAttachmentFilter, int twilightModifier) {
        AttachPermanentAction action = super.getPlayCardAction(playerId, game, self, additionalAttachmentFilter, twilightModifier);
        action.appendCost(
                new ChooseAndExertCharactersEffect(action, playerId, 1, 1, Culture.ISENGARD, CardType.MINION));
        return action;
    }

    @Override
    public Modifier getAlwaysOnModifier(LotroGame game, final PhysicalCard self) {
        return new PlayersCantPlayPhaseEventsOrPhaseSpecialAbilitiesModifier(self, new LocationCondition(Filters.hasAttached(self)), Phase.SKIRMISH);
    }

    @Override
    public List<RequiredTriggerAction> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult, PhysicalCard self) {
        if (TriggerConditions.endOfTurn(game, effectResult)) {
            RequiredTriggerAction action = new RequiredTriggerAction(self);
            action.appendEffect(new SelfDiscardEffect(self));

            return Collections.singletonList(action);
        }

        return null;
    }
}
