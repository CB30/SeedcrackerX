package kaptainwutax.seedcrackerX.finder;

import kaptainwutax.seedcrackerX.finder.decorator.*;
import kaptainwutax.seedcrackerX.finder.decorator.ore.EmeraldOreFinder;
import kaptainwutax.seedcrackerX.finder.structure.*;
import kaptainwutax.seedcrackerX.render.Renderer;
import kaptainwutax.seedcrackerX.util.HeightContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class Finder {

    protected static final List<BlockPos> CHUNK_POSITIONS = new ArrayList<>();
    protected static final List<BlockPos> SUB_CHUNK_POSITIONS = new ArrayList<>();
    protected static HeightContext heightContext;

    protected MinecraftClient mc = MinecraftClient.getInstance();
    protected List<Renderer> renderers = new ArrayList<>();
    protected World world;
    protected ChunkPos chunkPos;

    static {
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                for(int y = 0; y < 16; y++) {
                    SUB_CHUNK_POSITIONS.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    public Finder(World world, ChunkPos chunkPos) {
        this.world = world;
        this.chunkPos = chunkPos;
    }

    public World getWorld() {
        return this.world;
    }

    public ChunkPos getChunkPos() {
        return this.chunkPos;
    }

    public abstract List<BlockPos> findInChunk();

    public boolean shouldRender() {
        DimensionType finderDim = this.world.getDimension();
        DimensionType playerDim = mc.player.world.getDimension();

        if(finderDim != playerDim)return false;

        int renderDistance = mc.options.viewDistance * 16 + 16;
        Vec3d playerPos = mc.player.getPos();

        for(Renderer renderer: this.renderers) {
            BlockPos pos = renderer.getPos();
            double distance = playerPos.squaredDistanceTo(pos.getX(), playerPos.y, pos.getZ());
            if(distance <= renderDistance * renderDistance + 32)return true;
        }

        return false;
    }

    public void render(MatrixStack matrixStack, VertexConsumer vertexConsumer, Vec3d cameraPos) {
        this.renderers.forEach(renderer -> renderer.render(matrixStack, vertexConsumer, cameraPos));
    }

    public boolean isUseless() {
        return this.renderers.isEmpty();
    }

    public abstract boolean isValidDimension(DimensionType dimension);

    public boolean isOverworld(DimensionType dimension) {
        return ((DimensionTypeCaller)dimension).getInfiniburn().getPath().endsWith("overworld");
    }

    public boolean isNether(DimensionType dimension) {
        return ((DimensionTypeCaller)dimension).getInfiniburn().getPath().endsWith("nether");
    }

    public boolean isEnd(DimensionType dimension) {
        return ((DimensionTypeCaller)dimension).getInfiniburn().getPath().endsWith("end");
    }

    public static List<BlockPos> buildSearchPositions(List<BlockPos> base, Predicate<BlockPos> removeIf) {
        List<BlockPos> newList = new ArrayList<>();
        
        for(BlockPos pos: base) {
            if(!removeIf.test(pos)) {
                newList.add(pos);
            }
        }
        
        return newList;
    }

    public enum Category {
        STRUCTURES,
        DECORATORS,
        BIOMES,
    }

    public enum Type {
        BURIED_TREASURE(BuriedTreasureFinder::create, Category.STRUCTURES),
        DESERT_TEMPLE(DesertPyramidFinder::create, Category.STRUCTURES),
        END_CITY(EndCityFinder::create, Category.STRUCTURES),
        //IGLOO(IglooFinder::create, Category.STRUCTURES),
        JUNGLE_TEMPLE(JunglePyramidFinder::create, Category.STRUCTURES),
        MONUMENT(MonumentFinder::create, Category.STRUCTURES),
        SWAMP_HUT(SwampHutFinder::create, Category.STRUCTURES),
        //MANSION(MansionFinder::create, Category.STRUCTURES),
        SHIPWRECK(ShipwreckFinder::create, Category.STRUCTURES),

        END_PILLARS(EndPillarsFinder::create, Category.DECORATORS),
        END_GATEWAY(EndGatewayFinder::create, Category.DECORATORS),
        DUNGEON(DungeonFinder::create, Category.DECORATORS),
        EMERALD_ORE(EmeraldOreFinder::create, Category.DECORATORS),
        DESERT_WELL(DesertWellFinder::create, Category.DECORATORS),
        WARPED_FUNGUS(WarpedFungusFinder::create,Category.DECORATORS),
        BIOME(BiomeFinder::create, Category.BIOMES);

        public final FinderBuilder finderBuilder;
        private final Category category;

        Type(FinderBuilder finderBuilder, Category category) {
            this.finderBuilder = finderBuilder;
            this.category = category;
        }

        public static List<Type> getForCategory(Category category) {
            return Arrays.stream(values()).filter(type -> type.category == category).collect(Collectors.toList());
        }
    }

    public interface DimensionTypeCaller {
        Identifier getInfiniburn();
    }

}
