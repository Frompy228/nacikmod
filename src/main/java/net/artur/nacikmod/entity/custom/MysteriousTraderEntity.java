package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModBlocks;
import net.artur.nacikmod.entity.projectiles.ManaSwordProjectile;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import java.util.List;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;
import java.util.Random;
import java.util.EnumSet;

public class MysteriousTraderEntity extends PathfinderMob implements Merchant, MenuProvider {
    private Player tradingPlayer;
    private MerchantOffers offers = new MerchantOffers();
    private int villagerXp = 0;
    private int tradeLevel = 0; // Уровень торговли
    private static final int XP_PER_LEVEL = 10; // XP нужен для повышения уровня
    private boolean hasRareTrade = false; // Был ли сгенерирован редкий трейд
    private int tradeXp = 0; // Отдельный XP для торговли
    private boolean isDefending = false;
    private int defenseCooldown = 0;
    private static final int DEFENSE_COOLDOWN = 400;
    private LivingEntity lastAttacker = null;
    private static final int MAX_MANA = 1000;
    private int earthWallDelay = 0;
    private static final int EARTH_WALL_DELAY = 10; // 0.5 секунды (10 тиков)
    
    // Новые переменные для стрельбы и управления оружием
    private int rangedAttackCooldown = 0;
    private static final int RANGED_ATTACK_COOLDOWN = 100; // 5 секунд (20 тиков * 5)
    private static final float RANGED_ATTACK_DAMAGE = 10.0f;
    private boolean isInCombat = false;
    private int combatTimeout = 0;
    private static final int COMBAT_TIMEOUT = 200; // 10 секунд

    public MysteriousTraderEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(ModAttributes.BONUS_ARMOR.get(),15)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TraderRangedAttackGoal(this, 1.0));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new RestrictSunGoal(this));
        this.goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(this, 0.3D));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        
        // Инициализируем торговые предложения
        initializeOffers();
        this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });

        AttributeInstance attribute = this.getAttribute(ModAttributes.BONUS_ARMOR.get());
        attribute.setBaseValue(5.0);
        
        // Торговец не держит оружие по умолчанию
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        
        return data;
    }

    private void initializeOffers() {
        // Торговля: magic healing + god hand на hundred seal
        MerchantOffer offer = new MerchantOffer(
                new ItemStack(ModItems.MAGIC_HEALING.get(), 1),
                new ItemStack(ModItems.GOD_HAND.get(), 1),
                new ItemStack(ModItems.HUNDRED_SEAL.get(), 1),
                1, 1, 0.05F
        );
        offers.add(offer);

        // Второй трейд с шансом 10% (сохраняем информацию о нем)
        if (!hasRareTrade && this.random.nextFloat() < 0.1f) {
            hasRareTrade = true;
        }
        if (hasRareTrade) {
            ItemStack[] possibleRewards = {
                new ItemStack(ModItems.GRAVITY.get(), 1),
                new ItemStack(ModItems.INTANGIBILITY.get(), 1),
                new ItemStack(ModItems.POCKET.get(), 1),
                new ItemStack(ModItems.SENSORY_RAIN.get(), 1),
                new ItemStack(ModItems.ABSOLUTE_VISION.get(), 1)
            };
            ItemStack randomReward = possibleRewards[this.random.nextInt(possibleRewards.length)];
            MerchantOffer secondOffer = new MerchantOffer(
                    new ItemStack(ModItems.DARK_SPHERE.get(), 1),
                    new ItemStack(ModItems.SHARD_OF_ARTIFACT.get(), 1),
                    randomReward,
                    1, 1, 0.05F
            );
            offers.add(secondOffer);
        }
        // Третий трейд: 3 изумруда на 1 magic circuit (доступен всегда, 5 раз)
        MerchantOffer emeraldOffer = new MerchantOffer(
                new ItemStack(Items.EMERALD, 3),
                ItemStack.EMPTY,
                new ItemStack(ModItems.MAGIC_CIRCUIT.get(), 1),
                5, 2, 0.05F
        );
        offers.add(emeraldOffer);

        MerchantOffer slashOffer = new MerchantOffer(
                new ItemStack(ModItems.SLASH.get(), 1),
                new ItemStack(ModItems.SLASH.get(), 1),
                new ItemStack(ModItems.DOUBLE_SLASH.get(), 1),
                1, 2, 0.05F
        );
        offers.add(slashOffer);
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.IN_FIRE) || 
               source.is(net.minecraft.world.damagesource.DamageTypes.DROWN) ||
               source.is(net.minecraft.world.damagesource.DamageTypes.FALL) ||
               super.isInvulnerableTo(source);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // Торговец не получает урон от своих клонов
        if (source.getEntity() instanceof MysteriousTraderBattleCloneEntity) {
            return false;
        }
        
        // Торговец получает урон от всех остальных источников
        boolean hurt = super.hurt(source, amount);
        
        // Прекращаем торговлю при получении урона
        if (hurt && this.getTradingPlayer() != null) {
            this.stopTrading();
        }
        
        // При любой атаке устанавливаем атакующего и активируем бой
        if (hurt && this.isAlive() && source.getEntity() instanceof LivingEntity attacker) {
            this.lastAttacker = attacker;
            this.isInCombat = true;
            this.combatTimeout = COMBAT_TIMEOUT;
            
            // Сразу сбрасываем кулдаун стрельбы, чтобы начать стрелять
            this.rangedAttackCooldown = 0;
        }
        
        // Активируем защиту только если сущность жива, есть реальный урон и атакующий
        if (hurt && this.isAlive() && amount > 0 && defenseCooldown <= 0 && source.getEntity() instanceof LivingEntity attacker) {
            activateDefense(attacker);
        }
        
        return hurt;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && this.getTradingPlayer() == null && !player.isSecondaryUseActive()) {
            if (this.isBaby()) {
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            } else {
                boolean flag = this.getOffers().isEmpty();
                if (hand == InteractionHand.MAIN_HAND) {
                    player.awardStat(net.minecraft.stats.Stats.TALKED_TO_VILLAGER);
                }

                if (flag) {
                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                } else {
                    if (!this.level().isClientSide && !this.offers.isEmpty()) {
                        this.startTrading(player);
                    }

                    return InteractionResult.sidedSuccess(this.level().isClientSide);
                }
            }
        } else {
            return super.mobInteract(player, hand);
        }
    }

    private void startTrading(Player player) {
        this.setTradingPlayer(player);
        // Используем стандартный метод из интерфейса Merchant
        this.openTradingScreen(player, this.getDisplayName(), 1);
    }

    // Merchant interface implementation
    @Override
    public void setTradingPlayer(@Nullable Player player) {
        boolean flag = this.getTradingPlayer() != null && player == null;
        this.tradingPlayer = player;
        if (flag) {
            this.stopTrading();
        }
    }

    protected void stopTrading() {
        // Останавливаем торговлю
        this.setTradingPlayer(null);
    }

    @Override
    public void tick() {
        super.tick();
        
        // Обрабатываем кулдаун защиты
        if (defenseCooldown > 0) {
            defenseCooldown--;
        }
        
        // Обрабатываем кулдаун дальней атаки
        if (rangedAttackCooldown > 0) {
            rangedAttackCooldown--;
        }
        
        // Обрабатываем задержку создания земляной стены
        if (earthWallDelay > 0 && this.isAlive()) {
            earthWallDelay--;
            if (earthWallDelay <= 0) {
                createEarthWall();
            }
        }
        
        // Блокируем движение во время торговли и проверяем расстояние
        if (this.getTradingPlayer() != null) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            this.getNavigation().stop();
            
            // Прекращаем торговлю, если игрок отошел слишком далеко
            double distance = this.distanceToSqr(this.getTradingPlayer());
            if (distance > 9.0) {
                this.stopTrading();
            }
        }
        
        // Стрельба теперь управляется через Goal
        
        // Торговец смотрит на атакующего во время боя
        if (this.lastAttacker != null && this.lastAttacker.isAlive() && this.hasLineOfSight(this.lastAttacker)) {
            this.getLookControl().setLookAt(this.lastAttacker, 30.0F, 30.0F);
        }
        
        // Убираем атакующего, если он мертв или потерян
        if (this.lastAttacker != null && !this.lastAttacker.isAlive()) {
            this.lastAttacker = null;
        }
        
        // Обрабатываем таймаут боя
        if (combatTimeout > 0) {
            combatTimeout--;
            if (combatTimeout <= 0) {
                isInCombat = false;
                lastAttacker = null;
            }
        }
    }
    

    
    // Стрельба ManaSwordProjectile
    private void performRangedAttack() {
        if (this.level().isClientSide || !this.isAlive()) return;
        
        if (this.lastAttacker != null && this.lastAttacker.isAlive() && this.hasLineOfSight(this.lastAttacker)) {
            ManaSwordProjectile projectile = new ManaSwordProjectile(this.level(), this, RANGED_ATTACK_DAMAGE);
            projectile.setPos(this.getX(), this.getEyeY() - 0.1, this.getZ());
            
            // Вычисляем направление к цели
            double dx = this.lastAttacker.getX() - this.getX();
            double dy = this.lastAttacker.getEyeY() - this.getEyeY();
            double dz = this.lastAttacker.getZ() - this.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            // Нормализуем и устанавливаем скорость
            dx /= distance;
            dy /= distance;
            dz /= distance;
            
            projectile.setDeltaMovement(dx * 1.5, dy * 1.5, dz * 1.5);
            
            this.level().addFreshEntity(projectile);
            
            // Устанавливаем кулдаун
            this.rangedAttackCooldown = RANGED_ATTACK_COOLDOWN;
        }
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    @Override
    public MerchantOffers getOffers() {
        // Всегда инициализируем трейды, если они пустые
        if (this.offers == null || this.offers.isEmpty()) {
            this.offers = new MerchantOffers();
            this.initializeOffers();
        }
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {
        if (offers != null) {
            this.offers = offers;
        }
    }

    private static final int[] LEVEL_XP = {0, XP_PER_LEVEL}; // 0-9: уровень 0, 10+: уровень 1

    public int getVillagerLevel() {
        if (tradeXp >= LEVEL_XP[1]) return 1;
        return 0;
    }

    @Override
    public int getVillagerXp() {
        return this.tradeXp;
    }

    // Открываем новый трейд только при достижении нужного XP
    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.tradeXp += offer.getXp();
        // Проверяем, нужно ли открыть новый трейд (уровень 1)
        if (getVillagerLevel() == 1 && this.tradeLevel == 0) {
            this.tradeLevel = 1;
            MerchantOffer charmOffer = new MerchantOffer(
                new ItemStack(ModItems.MAGIC_CHARM.get(), 1),
                ItemStack.EMPTY,
                new ItemStack(ModItems.MAGIC_CIRCUIT.get(), 7),
                1, 1, 0.05F // 1 XP за трейд
            );
            this.offers.add(charmOffer);

            this.tradeLevel = 1;
            MerchantOffer ancientWorldSlashOffer = new MerchantOffer(
                    new ItemStack(ModItems.ANCIENT_SCROLL.get(), 1),
                    ItemStack.EMPTY,
                    new ItemStack(ModItems.ANCIENT_SEAL.get(), 1),
                    1, 2, 0.05F // 2 XP за трейд
            );
            this.offers.add(ancientWorldSlashOffer);

            if (this.level() != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }
        if (this.level() != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        // Можно добавить дополнительную логику при обновлении торговли
    }

    @Override
    public boolean showProgressBar() {
        return true; // Показываем прогресс-бар для отображения уровня торговли
    }

    // Прогресс-бар считает прогресс между уровнями
    public int getTradeProgress() {
        int level = getVillagerLevel();
        if (level == 0) {
            return Math.min(this.tradeXp, XP_PER_LEVEL);
        } else {
            // На максимальном уровне прогресс-бар всегда полный
            return XP_PER_LEVEL;
        }
    }

    public int getMaxTradeProgress() {
        return XP_PER_LEVEL;
    }

    @Override
    public boolean canRestock() {
        return false; // Наш торговец не может пополнять запасы
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public void overrideXp(int xp) {
        // Игнорируем автоматическую синхронизацию XP системой Minecraft
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Offers", this.offers.createTag());
        tag.putInt("TradeLevel", this.tradeLevel);
        tag.putInt("TradeXp", this.tradeXp);
        tag.putBoolean("HasRareTrade", this.hasRareTrade);
        tag.putBoolean("IsDefending", this.isDefending);
        tag.putInt("DefenseCooldown", this.defenseCooldown);
        tag.putInt("EarthWallDelay", this.earthWallDelay);
        tag.putInt("RangedAttackCooldown", this.rangedAttackCooldown);
        tag.putBoolean("IsInCombat", this.isInCombat);
        tag.putInt("CombatTimeout", this.combatTimeout);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Offers", 10)) {
            this.offers = new MerchantOffers(tag.getCompound("Offers"));
        } else {
            // Если предложения не найдены, инициализируем их
            this.initializeOffers();
        }
        this.tradeLevel = tag.getInt("TradeLevel");
        this.tradeXp = tag.getInt("TradeXp");
        this.hasRareTrade = tag.getBoolean("HasRareTrade");
        this.isDefending = tag.getBoolean("IsDefending");
        this.defenseCooldown = tag.getInt("DefenseCooldown");
        this.earthWallDelay = tag.getInt("EarthWallDelay");
        this.rangedAttackCooldown = tag.getInt("RangedAttackCooldown");
        this.isInCombat = tag.getBoolean("IsInCombat");
        this.combatTimeout = tag.getInt("CombatTimeout");
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHit) {
        Random random = new Random(); // Генератор случайных чисел
        double chanceSlash = 0.10;

        if (random.nextDouble() < chanceSlash) {
            this.spawnAtLocation(new ItemStack(ModItems.SLASH.get(), 1));
        }
    }

    @Override
    protected void dropEquipment() {
        // Торговец не дропает снаряжение при смерти
    }

    @Override
    protected void dropExperience() {
        // Торговец не дропает опыт при смерти
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof MysteriousTraderEntity || entity instanceof MysteriousTraderBattleCloneEntity) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.nacikmod.mysterious_trader");
    }

    // MenuProvider interface implementation
    @Override
    public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
        MerchantMenu menu = new MerchantMenu(containerId, playerInventory, this);
        return menu;
    }

    private void activateDefense(LivingEntity attacker) {
        if (this.level().isClientSide || !this.isAlive()) return;
        
        this.lastAttacker = attacker;
        this.defenseCooldown = DEFENSE_COOLDOWN;
        
        // Отбрасываем атакующих в радиусе 4 блоков
        knockbackNearbyEntities();
        
        // Проигрываем звук защиты
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        
        // Запускаем таймер для создания земляной стены через 0.5 секунды
        this.earthWallDelay = EARTH_WALL_DELAY;
        
        // Создаем боевых клонов вокруг атакующего
        spawnBattleClones(attacker);
    }
    
    private void knockbackNearbyEntities() {
        AABB area = new AABB(
                this.getX() - 4, this.getY() - 4, this.getZ() - 4,
                this.getX() + 4, this.getY() + 4, this.getZ() + 4
        );
        
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);
        
        for (LivingEntity entity : entities) {
            if (entity != this && entity.isAlive()) {
                // Вычисляем направление от торговца к сущности
                Vec3 toEntity = entity.position().subtract(this.position());
                double distance = toEntity.length();
                
                if (distance > 0) {
                    // Нормализуем и увеличиваем силу отбрасывания
                    Vec3 knockbackDir = toEntity.normalize();
                    Vec3 knockback = knockbackDir.scale(4.0); // Увеличиваем силу
                    
                    // Добавляем вертикальный компонент
                    knockback = knockback.add(0, 1.0, 0);
                    
                    // Применяем отбрасывание
                    entity.setDeltaMovement(knockback);
                    
                    // Наносим небольшой урон для эффекта
                    entity.hurt(this.damageSources().mobAttack(this), 2.0f);
                    
                    // Проигрываем звук отбрасывания
                    this.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.NEUTRAL, 1.0F, 0.8F);
                }
            }
        }
    }
    
    private void createEarthWall() {
        if (this.level().isClientSide || !this.isAlive()) return;
        
        BlockPos center = this.blockPosition();
        int radius = 3; // Увеличиваем радиус для лучшей защиты
        
        // Создаем защитную стену вокруг торговца
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                
                // Создаем кольцо блоков (не заполняем центр)
                if (distance >= radius - 0.5 && distance <= radius + 0.5) {
                    BlockPos pos = center.offset(x, 0, z);
                    
                    // Проверяем, что блок можно заменить
                    if (this.level().getBlockState(pos).isAir()) {
                        this.level().setBlockAndUpdate(pos, ModBlocks.TEMPORARY_DIRT.get().defaultBlockState());
                    }
                    
                    // Добавляем второй слой для большей высоты стены
                    BlockPos posAbove = pos.above();
                    if (this.level().getBlockState(posAbove).isAir()) {
                        this.level().setBlockAndUpdate(posAbove, ModBlocks.TEMPORARY_DIRT.get().defaultBlockState());
                    }
                }
            }
        }
        
        // Проигрываем звук создания стены
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ROOTED_DIRT_PLACE, SoundSource.NEUTRAL, 1.0F, 0.8F);
        
        // Добавляем частицы для эффекта
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double x = this.getX() + (this.random.nextDouble() - 0.5) * 6;
                double z = this.getZ() + (this.random.nextDouble() - 0.5) * 6;
                serverLevel.sendParticles(
                        new net.minecraft.core.particles.BlockParticleOption(
                                net.minecraft.core.particles.ParticleTypes.BLOCK,
                                ModBlocks.TEMPORARY_DIRT.get().defaultBlockState()),
                        x, this.getY(), z, 1, 0, 0.5, 0, 0.1);
            }
        }
    }
    
    private void spawnBattleClones(LivingEntity attacker) {
        if (this.level().isClientSide || !this.isAlive()) return;
        
        // Спавним 3 боевых клона вокруг атакующего
        for (int i = 0; i < 3; i++) {
            double angle = (i * 120) * Math.PI / 180; // 120 градусов между клонами
            double distance = 2.5;
            
            double x = attacker.getX() + Math.cos(angle) * distance;
            double z = attacker.getZ() + Math.sin(angle) * distance;
            double y = attacker.getY();
            
            MysteriousTraderBattleCloneEntity clone = ModEntities.MYSTERIOUS_TRADER_BATTLE_CLONE.get().create(this.level());
            if (clone != null) {
                clone.setPos(x, y, z);
                clone.setTarget(attacker);
                
                // Настраиваем клона для защиты этого торговца
                clone.protectedTrader = this;
                
                clone.finalizeSpawn((ServerLevelAccessor) this.level(), 
                        this.level().getCurrentDifficultyAt(clone.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null, null);
                this.level().addFreshEntity(clone);
            }
        }
    }

    public static boolean canSpawn(EntityType<MysteriousTraderEntity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        // Проверка времени суток: день
        long timeOfDay = world.getLevel().getDayTime() % 24000L;
        boolean isDay = timeOfDay >= 0 && timeOfDay < 12000;

        // Проверка освещения
        int lightLevel = world.getLevel().getMaxLocalRawBrightness(pos);
        boolean lightOk = lightLevel >= 5;

        // Проверка блока под мобом
        boolean groundSolid = world.getBlockState(pos.below()).isSolid();

        return isDay && lightOk && groundSolid;
    }

    // Goal для стрельбы торговца
    static class TraderRangedAttackGoal extends Goal {
        private final MysteriousTraderEntity trader;
        private final double speedModifier;

        public TraderRangedAttackGoal(MysteriousTraderEntity trader, double speedModifier) {
            this.trader = trader;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return trader.lastAttacker != null && trader.lastAttacker.isAlive() && 
                   trader.rangedAttackCooldown <= 0 && trader.hasLineOfSight(trader.lastAttacker) &&
                   !(trader.lastAttacker instanceof MysteriousTraderBattleCloneEntity);
        }

        @Override
        public boolean canContinueToUse() {
            return trader.lastAttacker != null && trader.lastAttacker.isAlive() &&
                   !(trader.lastAttacker instanceof MysteriousTraderBattleCloneEntity);
        }

        @Override
        public void start() {
            // Берем оружие когда начинаем стрелять
            trader.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.MAGIC_WEAPONS.get()));
        }

        @Override
        public void tick() {
            if (trader.lastAttacker != null && trader.lastAttacker.isAlive() && 
                !(trader.lastAttacker instanceof MysteriousTraderBattleCloneEntity)) {
                // Смотрим на цель
                trader.getLookControl().setLookAt(trader.lastAttacker, 30.0F, 30.0F);
                
                // Стреляем если можем
                if (trader.rangedAttackCooldown <= 0 && trader.hasLineOfSight(trader.lastAttacker)) {
                    trader.performRangedAttack();
                }
            }
        }

        @Override
        public void stop() {
            // Убираем оружие когда перестаем стрелять
            trader.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }
}
