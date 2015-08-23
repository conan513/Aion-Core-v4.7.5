/**
 * This file is part of Aion-Lightning <aion-lightning.org>.
 *
 *  Aion-Lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Aion-Lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details. *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Aion-Lightning.
 *  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Credits goes to all Open Source Core Developer Groups listed below
 * Please do not change here something, ragarding the developer credits, except the "developed by XXXX".
 * Even if you edit a lot of files in this source, you still have no rights to call it as "your Core".
 * Everybody knows that this Emulator Core was developed by Aion Lightning 
 * @-Aion-Unique-
 * @-Aion-Lightning
 * @Aion-Engine
 * @Aion-Extreme
 * @Aion-NextGen
 * @Aion-Core Dev.
 */
package ai.instance.voidCube;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.AttackIntention;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.ai2.handler.*;
import com.aionemu.gameserver.ai2.manager.SkillAttackManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.skill.NpcSkillEntry;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.templates.npcshout.ShoutEventType;
import com.aionemu.gameserver.services.SiegeService;

/**
 * @author DeathMagnestic
 */
@AIName("legion_voidcube_portal")
public class LegionVoidCubPortalAI2 extends NpcAI2 {

    @Override
    public void think() {
        ThinkEventHandler.onThink(this);
    }

    @Override
    protected void handleDied() {
        DiedEventHandler.onDie(this);
    }

    @Override
    protected void handleAttack(Creature creature) {
        AttackEventHandler.onAttack(this, creature);
    }

    @Override
    protected boolean handleCreatureNeedsSupport(Creature creature) {
        return AggroEventHandler.onCreatureNeedsSupport(this, creature);
    }

    @Override
    protected void handleDialogStart(Player player) {
        final SiegeLocation loc = SiegeService.getInstance().getSiegeLocation(5011);
        Legion playerlegion = player.getLegion();
        if (player.getLegion() != null) {
            if (playerlegion.getLegionId() == loc.getLegionId()) {
                TalkEventHandler.onTalk(this, player);
            }
        } else {
            TalkEventHandler.onFinishTalk(this, player);
        }
    }

    @Override
    protected void handleDialogFinish(Player creature) {
        TalkEventHandler.onFinishTalk(this, creature);
    }

    @Override
    protected void handleFinishAttack() {
        AttackEventHandler.onFinishAttack(this);
    }

    @Override
    protected void handleAttackComplete() {
        AttackEventHandler.onAttackComplete(this);
    }

    @Override
    protected void handleTargetReached() {
        TargetEventHandler.onTargetReached(this);
    }

    @Override
    protected void handleNotAtHome() {
        ReturningEventHandler.onNotAtHome(this);
    }

    @Override
    protected void handleBackHome() {
        ReturningEventHandler.onBackHome(this);
    }

    @Override
    protected void handleTargetTooFar() {
        TargetEventHandler.onTargetTooFar(this);
    }

    @Override
    protected void handleTargetGiveup() {
        TargetEventHandler.onTargetGiveup(this);
    }

    @Override
    protected void handleTargetChanged(Creature creature) {
        super.handleTargetChanged(creature);
        TargetEventHandler.onTargetChange(this, creature);
    }

    @Override
    protected void handleMoveValidate() {
        MoveEventHandler.onMoveValidate(this);
    }

    @Override
    protected void handleMoveArrived() {
        super.handleMoveArrived();
        MoveEventHandler.onMoveArrived(this);
    }

    @Override
    protected void handleCreatureMoved(Creature creature) {
        CreatureEventHandler.onCreatureMoved(this, creature);
    }

    @Override
    protected void handleDespawned() {
        super.handleDespawned();
    }

    @Override
    protected boolean canHandleEvent(AIEventType eventType) {
        boolean canHandle = super.canHandleEvent(eventType);

        switch (eventType) {
            case CREATURE_MOVED:
                return canHandle
                        || DataManager.NPC_SHOUT_DATA.hasAnyShout(getOwner().getWorldId(), getOwner().getNpcId(), ShoutEventType.SEE);
            case CREATURE_NEEDS_SUPPORT:
                return canHandle
                        && isNonFightingState()
                        && DataManager.TRIBE_RELATIONS_DATA.hasSupportRelations(getOwner().getTribe());
        }
        return canHandle;
    }

    @Override
    public AttackIntention chooseAttackIntention() {
        VisibleObject currentTarget = getTarget();
        Creature mostHated = getAggroList().getMostHated();

        if (mostHated == null || mostHated.getLifeStats().isAlreadyDead()) {
            return AttackIntention.FINISH_ATTACK;
        }

        if (currentTarget == null || !currentTarget.getObjectId().equals(mostHated.getObjectId())) {
            onCreatureEvent(AIEventType.TARGET_CHANGED, mostHated);
            return AttackIntention.SWITCH_TARGET;
        }

        if (getOwner().getObjectTemplate().getAttackRange() == 0) {
            NpcSkillEntry skill = getOwner().getSkillList().getRandomSkill();
            if (skill != null) {
                skillId = skill.getSkillId();
                skillLevel = skill.getSkillLevel();
                return AttackIntention.SKILL_ATTACK;
            }
        } else {
            NpcSkillEntry skill = SkillAttackManager.chooseNextSkill(this);
            if (skill != null) {
                skillId = skill.getSkillId();
                skillLevel = skill.getSkillLevel();
                return AttackIntention.SKILL_ATTACK;
            }
        }
        return AttackIntention.SIMPLE_ATTACK;
    }
}