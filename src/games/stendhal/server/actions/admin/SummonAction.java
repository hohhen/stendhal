package games.stendhal.server.actions.admin;

import static games.stendhal.server.actions.WellKnownActionConstants.X;
import static games.stendhal.server.actions.WellKnownActionConstants.Y;
import games.stendhal.common.Grammar;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.RaidCreature;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class SummonAction extends AdministrationAction {
	private static final String USAGE = "Usage: /summon <whatToSummon> [<x> <y>]";
	private static final String _CREATURE = "creature";
	private static final String _SUMMON = "summon";

	public static void register() {
		CommandCenter.register(_SUMMON, new SummonAction(), 800);
	}

	@Override
	public void perform(Player player, RPAction action) {
		try {
			if (action.has(_CREATURE) && action.has(X) && action.has(Y)) {
				StendhalRPZone zone = player.getZone();
				int x = action.getInt(X);
				int y = action.getInt(Y);

				if (!zone.collides(player, x, y)) {
					EntityManager manager = SingletonRepository.getEntityManager();
					String type = action.get(_CREATURE);

					Entity entity = manager.getEntity(type);

					// see it the name was in plural
					if (entity == null) {
						type = Grammar.singular(type);

						entity = manager.getEntity(type);
					}

					if (entity == null) {
						logger.info("onSummon: Entity \"" + type + "\" not found.");
						player.sendPrivateText("onSummon: Entity \"" + type + "\" not found.");
						return;
					} else if (manager.isCreature(type)) {
						entity = new RaidCreature((Creature) entity);
					}

					SingletonRepository.getRuleProcessor().addGameEvent(player.getName(), _SUMMON, type);

					StendhalRPAction.placeat(zone, entity, x, y);
				}
			}
		} catch (NumberFormatException e) {
			player.sendPrivateText(USAGE);
		}
	}

}
