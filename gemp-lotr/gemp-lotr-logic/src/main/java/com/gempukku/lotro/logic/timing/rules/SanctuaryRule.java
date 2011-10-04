package com.gempukku.lotro.logic.timing.rules;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.game.AbstractActionProxy;
import com.gempukku.lotro.game.state.LotroGame;
import com.gempukku.lotro.game.state.actions.DefaultActionsEnvironment;
import com.gempukku.lotro.logic.actions.RequiredTriggerAction;
import com.gempukku.lotro.logic.effects.ChooseAndHealCharactersEffect;
import com.gempukku.lotro.logic.timing.Action;
import com.gempukku.lotro.logic.timing.EffectResult;

import java.util.Collections;
import java.util.List;

public class SanctuaryRule {
    private DefaultActionsEnvironment _actionsEnvironment;

    public SanctuaryRule(DefaultActionsEnvironment actionsEnvironment) {
        _actionsEnvironment = actionsEnvironment;
    }

    public void applyRule() {
        _actionsEnvironment.addAlwaysOnActionProxy(
                new AbstractActionProxy() {
                    @Override
                    public List<? extends Action> getRequiredAfterTriggers(LotroGame game, EffectResult effectResult) {
                        if (effectResult.getType() == EffectResult.Type.START_OF_TURN
                                && game.getModifiersQuerying().hasKeyword(game.getGameState(), game.getGameState().getCurrentSite(), Keyword.SANCTUARY)) {
                            RequiredTriggerAction action = new RequiredTriggerAction(null) {
                                @Override
                                public String getText(LotroGame game) {
                                    return "Sanctuary healing";
                                }
                            };
                            for (int i = 0; i < 5; i++) {
                                final int remainingHeals = 5 - i;
                                ChooseAndHealCharactersEffect healEffect = new ChooseAndHealCharactersEffect(action, game.getGameState().getCurrentPlayerId(), 0, 1, Filters.type(CardType.COMPANION));
                                healEffect.setChoiceText("Sanctuary healing - Choose companion to heal - remaining heals: " + remainingHeals);
                                action.appendEffect(healEffect);
                            }
                            return Collections.singletonList(action);
                        }
                        return null;
                    }
                });
    }
}
