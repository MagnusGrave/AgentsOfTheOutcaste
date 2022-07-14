package gameLogic;

//The destination station for all item sheet data thats been converted to source code for easy precompiled referencing
public class ItemDepot {
	public class ItemProxy {
		protected ItemProxy(String id) {
			this.id = id;
		}
		private String id;
		public String getId() { return id; }
	}
	
	private static ItemDepot instance = new ItemDepot();
	
	// <- Weapon Items
	
	//Sample format
	//public class OldBokuto extends ItemProxy { public OldBokuto(int id) { super(id); } }
	//public static OldBokuto OldBokuto = instance.new OldBokuto(794701635);
	
	//Actual Data
	public class OldBokuto extends ItemProxy { public OldBokuto(String id) { super(id); } }
	public static OldBokuto OldBokuto = instance.new OldBokuto("b9afe6d1-85d9-4f53-b79b-ecbc4d381b23");
	public class PineBokuto extends ItemProxy { public PineBokuto(String id) { super(id); } }
	public static PineBokuto PineBokuto = instance.new PineBokuto("8e4ade3e-f7ea-4dba-b8e9-ea01cec70905");
	public class CherryBokuto extends ItemProxy { public CherryBokuto(String id) { super(id); } }
	public static CherryBokuto CherryBokuto = instance.new CherryBokuto("20aa8f76-d478-4dab-83e8-dd29de028e2f");
	public class WornPadoru extends ItemProxy { public WornPadoru(String id) { super(id); } }
	public static WornPadoru WornPadoru = instance.new WornPadoru("287ed6ee-1415-4da1-af13-60341d5f09bf");
	public class CraftsmanPadoru extends ItemProxy { public CraftsmanPadoru(String id) { super(id); } }
	public static CraftsmanPadoru CraftsmanPadoru = instance.new CraftsmanPadoru("8167cc16-6c61-4e45-bf44-adaa6cbc5af8");
	public class Shinai extends ItemProxy { public Shinai(String id) { super(id); } }
	public static Shinai Shinai = instance.new Shinai("84212926-c5f4-4acc-acda-b352b5bfaa35");
	public class PlainBreaker extends ItemProxy { public PlainBreaker(String id) { super(id); } }
	public static PlainBreaker PlainBreaker = instance.new PlainBreaker("e3e45843-507e-4c9e-91ea-4f6985fdf433");
	public class HardenedBreaker extends ItemProxy { public HardenedBreaker(String id) { super(id); } }
	public static HardenedBreaker HardenedBreaker = instance.new HardenedBreaker("2be95e14-c0c7-48bc-82a5-f4eabff527bc");
	public class BrassBoMace extends ItemProxy { public BrassBoMace(String id) { super(id); } }
	public static BrassBoMace BrassBoMace = instance.new BrassBoMace("8e452f92-6518-4b4c-abfa-85e8066603f9");
	public class SilverBoMace extends ItemProxy { public SilverBoMace(String id) { super(id); } }
	public static SilverBoMace SilverBoMace = instance.new SilverBoMace("17a02116-5789-4104-805f-f4f3bac4cdde");
	public class TungstenBoMace extends ItemProxy { public TungstenBoMace(String id) { super(id); } }
	public static TungstenBoMace TungstenBoMace = instance.new TungstenBoMace("5c1846d2-4004-4fe9-ae48-8b9d0946aa39");
	public class SteelKanabo extends ItemProxy { public SteelKanabo(String id) { super(id); } }
	public static SteelKanabo SteelKanabo = instance.new SteelKanabo("ebd952b4-18a8-401a-b3a2-d16b090b21ed");
	public class AlloyKanabo extends ItemProxy { public AlloyKanabo(String id) { super(id); } }
	public static AlloyKanabo AlloyKanabo = instance.new AlloyKanabo("5e4a98a7-4158-49d8-93c4-96bfcc99f24c");
	public class IronOniMace extends ItemProxy { public IronOniMace(String id) { super(id); } }
	public static IronOniMace IronOniMace = instance.new IronOniMace("2ab4b5f4-e89b-4683-8b93-0cca7031034f");
	public class GoldOniMace extends ItemProxy { public GoldOniMace(String id) { super(id); } }
	public static GoldOniMace GoldOniMace = instance.new GoldOniMace("48d9db4c-7909-47e4-85f3-0b6be160f0e2");
	public class MountainsFury extends ItemProxy { public MountainsFury(String id) { super(id); } }
	public static MountainsFury MountainsFury = instance.new MountainsFury("f9892c7c-66cf-4f0e-8a03-2293b7cab50e");
	public class CommonJitte extends ItemProxy { public CommonJitte(String id) { super(id); } }
	public static CommonJitte CommonJitte = instance.new CommonJitte("7ccc653d-5023-43ec-80ea-443865f070b3");
	public class RefinedJitte extends ItemProxy { public RefinedJitte(String id) { super(id); } }
	public static RefinedJitte RefinedJitte = instance.new RefinedJitte("0b5b5c53-2bc3-44af-93ca-c2295ca4ff04");
	public class ScaldingRebuke extends ItemProxy { public ScaldingRebuke(String id) { super(id); } }
	public static ScaldingRebuke ScaldingRebuke = instance.new ScaldingRebuke("fcfd8bc2-1c75-4819-9887-a1a69c8bbf6d");
	public class LongJitte extends ItemProxy { public LongJitte(String id) { super(id); } }
	public static LongJitte LongJitte = instance.new LongJitte("ef007c23-46b7-46bb-a6aa-5cf4c68581bc");
	public class OfficersJitte extends ItemProxy { public OfficersJitte(String id) { super(id); } }
	public static OfficersJitte OfficersJitte = instance.new OfficersJitte("5522cdf4-043d-4d8e-9720-bb7fbdc3452a");
	public class PaperFan extends ItemProxy { public PaperFan(String id) { super(id); } }
	public static PaperFan PaperFan = instance.new PaperFan("f09c9e66-5e3b-41fa-8bb1-95f0f82b5240");
	public class FinePaperFan extends ItemProxy { public FinePaperFan(String id) { super(id); } }
	public static FinePaperFan FinePaperFan = instance.new FinePaperFan("4496a83a-b07a-40e0-9047-2081621c7c60");
	public class QualityPaperFan extends ItemProxy { public QualityPaperFan(String id) { super(id); } }
	public static QualityPaperFan QualityPaperFan = instance.new QualityPaperFan("f24ee004-9777-4bd7-bd9d-62e969d9362f");
	public class RarePaperFan extends ItemProxy { public RarePaperFan(String id) { super(id); } }
	public static RarePaperFan RarePaperFan = instance.new RarePaperFan("3680db47-e6e8-44e3-854b-b1951a721073");
	public class IronFan extends ItemProxy { public IronFan(String id) { super(id); } }
	public static IronFan IronFan = instance.new IronFan("fd3e5b51-ee7d-4d1b-adc2-561d85875f25");
	public class GoldFan extends ItemProxy { public GoldFan(String id) { super(id); } }
	public static GoldFan GoldFan = instance.new GoldFan("f873aa47-46ef-4d2f-ba10-1c7682a11632");
	public class SlatFan extends ItemProxy { public SlatFan(String id) { super(id); } }
	public static SlatFan SlatFan = instance.new SlatFan("a0d526b7-e9b7-440c-9bbb-05745509adef");
	public class ExquisiteSlatFan extends ItemProxy { public ExquisiteSlatFan(String id) { super(id); } }
	public static ExquisiteSlatFan ExquisiteSlatFan = instance.new ExquisiteSlatFan("210789e2-056a-42e4-a543-ba7378ab065c");
	public class RedWing extends ItemProxy { public RedWing(String id) { super(id); } }
	public static RedWing RedWing = instance.new RedWing("881478a9-eb04-44c3-850a-d898224e27e7");
	public class EmeraldWing extends ItemProxy { public EmeraldWing(String id) { super(id); } }
	public static EmeraldWing EmeraldWing = instance.new EmeraldWing("59ff5a86-b76a-4cfa-8784-f846f03f1944");
	public class PaleWing extends ItemProxy { public PaleWing(String id) { super(id); } }
	public static PaleWing PaleWing = instance.new PaleWing("7922ab01-c10e-4dcc-af63-731e27608669");
	public class DarkWing extends ItemProxy { public DarkWing(String id) { super(id); } }
	public static DarkWing DarkWing = instance.new DarkWing("68ab2496-8a57-49ed-8328-dd809e1e4fc4");
	public class KamisBreath extends ItemProxy { public KamisBreath(String id) { super(id); } }
	public static KamisBreath KamisBreath = instance.new KamisBreath("24133007-2f7d-42fe-83ee-730abf518756");
	public class YokaisBreath extends ItemProxy { public YokaisBreath(String id) { super(id); } }
	public static YokaisBreath YokaisBreath = instance.new YokaisBreath("341f5065-75f2-415f-8d73-0982f1d534a0");
	public class Shide extends ItemProxy { public Shide(String id) { super(id); } }
	public static Shide Shide = instance.new Shide("bbd72518-12b3-462c-b6ca-0793da2506a7");
	public class BrightShide extends ItemProxy { public BrightShide(String id) { super(id); } }
	public static BrightShide BrightShide = instance.new BrightShide("261024e7-fad3-4e33-af77-35663d0e091b");
	public class Shakujo extends ItemProxy { public Shakujo(String id) { super(id); } }
	public static Shakujo Shakujo = instance.new Shakujo("e6ad6a98-c4e5-499a-b066-9d072517a9cd");
	public class GoldShakujo extends ItemProxy { public GoldShakujo(String id) { super(id); } }
	public static GoldShakujo GoldShakujo = instance.new GoldShakujo("bc26070a-a2de-404b-9e91-351f1d70e9a9");
	public class VoidShakujo extends ItemProxy { public VoidShakujo(String id) { super(id); } }
	public static VoidShakujo VoidShakujo = instance.new VoidShakujo("8f22262a-c413-4990-bf8c-563e07c5a840");
	public class RustyBareBlade extends ItemProxy { public RustyBareBlade(String id) { super(id); } }
	public static RustyBareBlade RustyBareBlade = instance.new RustyBareBlade("c2ab838d-2a9e-40f9-9e27-44c97f750104");
	public class BareBlade extends ItemProxy { public BareBlade(String id) { super(id); } }
	public static BareBlade BareBlade = instance.new BareBlade("fc300335-d52e-49fb-b2db-c6117e5ed922");
	public class Katana extends ItemProxy { public Katana(String id) { super(id); } }
	public static Katana Katana = instance.new Katana("f11af72a-f6e2-4704-94a7-6fddd1bafb2c");
	public class RiversEdge extends ItemProxy { public RiversEdge(String id) { super(id); } }
	public static RiversEdge RiversEdge = instance.new RiversEdge("c95a3401-0835-4741-b049-f992dcce3520");
	public class NightsEdge extends ItemProxy { public NightsEdge(String id) { super(id); } }
	public static NightsEdge NightsEdge = instance.new NightsEdge("62220b93-c0d4-4c0f-9351-e0420c58899c");
	public class MoonsRadiance extends ItemProxy { public MoonsRadiance(String id) { super(id); } }
	public static MoonsRadiance MoonsRadiance = instance.new MoonsRadiance("3a2a8d7c-d534-4159-900f-9001320e5961");
	public class FineKatana extends ItemProxy { public FineKatana(String id) { super(id); } }
	public static FineKatana FineKatana = instance.new FineKatana("4368676d-446e-4967-9bdc-71a3418e46c4");
	public class BlessedBlade extends ItemProxy { public BlessedBlade(String id) { super(id); } }
	public static BlessedBlade BlessedBlade = instance.new BlessedBlade("193e0dc1-8da9-495c-8004-79e932bf18bf");
	public class SinkingBlade extends ItemProxy { public SinkingBlade(String id) { super(id); } }
	public static SinkingBlade SinkingBlade = instance.new SinkingBlade("e164b4e3-3150-488b-9a13-f6b1e4a74935");
	public class SeethingBlade extends ItemProxy { public SeethingBlade(String id) { super(id); } }
	public static SeethingBlade SeethingBlade = instance.new SeethingBlade("b3d5704c-7a68-4ab1-ad00-333eb1416188");
	public class KamisRage extends ItemProxy { public KamisRage(String id) { super(id); } }
	public static KamisRage KamisRage = instance.new KamisRage("b6278da8-67c0-46a8-91b5-25051f92301d");
	public class KamisWraith extends ItemProxy { public KamisWraith(String id) { super(id); } }
	public static KamisWraith KamisWraith = instance.new KamisWraith("b0b62ea1-4285-439d-8fee-c3b4ec661642");
	public class PhantomBlade extends ItemProxy { public PhantomBlade(String id) { super(id); } }
	public static PhantomBlade PhantomBlade = instance.new PhantomBlade("ad03d02a-aef5-4851-a3d4-350e2c1dc940");
	public class MasterofFire extends ItemProxy { public MasterofFire(String id) { super(id); } }
	public static MasterofFire MasterofFire = instance.new MasterofFire("aa2ad725-7b11-42da-ab16-2c63e473e885");
	public class MasterofWater extends ItemProxy { public MasterofWater(String id) { super(id); } }
	public static MasterofWater MasterofWater = instance.new MasterofWater("63a82950-8c2f-4b62-a048-810f7878e0b5");
	public class MasterofEarth extends ItemProxy { public MasterofEarth(String id) { super(id); } }
	public static MasterofEarth MasterofEarth = instance.new MasterofEarth("dd980fb0-69c6-43e5-84f5-07ee680fa9b0");
	public class Dai extends ItemProxy { public Dai(String id) { super(id); } }
	public static Dai Dai = instance.new Dai("8d825f7e-2bb2-4ef3-9804-76e109ffb4fa");
	public class ThinDai extends ItemProxy { public ThinDai(String id) { super(id); } }
	public static ThinDai ThinDai = instance.new ThinDai("6b6a6d30-85f9-4e0c-a3ce-58fbb09731d3");
	public class DarkDai extends ItemProxy { public DarkDai(String id) { super(id); } }
	public static DarkDai DarkDai = instance.new DarkDai("094c6c6f-185c-4089-a5dd-aebb5d4b465e");
	public class RefinedDai extends ItemProxy { public RefinedDai(String id) { super(id); } }
	public static RefinedDai RefinedDai = instance.new RefinedDai("e1c1eddc-1ba4-4cee-8032-55710943533b");
	public class FlameDai extends ItemProxy { public FlameDai(String id) { super(id); } }
	public static FlameDai FlameDai = instance.new FlameDai("fc6c7299-a395-4a66-b90c-7a57c8f71dff");
	public class EtchedFlameDai extends ItemProxy { public EtchedFlameDai(String id) { super(id); } }
	public static EtchedFlameDai EtchedFlameDai = instance.new EtchedFlameDai("24fa1dc2-e4a1-4b42-8f2d-79b11168407b");
	public class FlameEmbodied extends ItemProxy { public FlameEmbodied(String id) { super(id); } }
	public static FlameEmbodied FlameEmbodied = instance.new FlameEmbodied("3f7f2eed-acd7-45c3-b78e-124427ac1b79");
	public class WindDai extends ItemProxy { public WindDai(String id) { super(id); } }
	public static WindDai WindDai = instance.new WindDai("a5c6a380-a8d6-4ee0-ade9-bf074bc30318");
	public class EtchedWindDai extends ItemProxy { public EtchedWindDai(String id) { super(id); } }
	public static EtchedWindDai EtchedWindDai = instance.new EtchedWindDai("1eb00372-d502-4d0b-a0db-12131180e054");
	public class WindEmbodied extends ItemProxy { public WindEmbodied(String id) { super(id); } }
	public static WindEmbodied WindEmbodied = instance.new WindEmbodied("e78f1e2d-f0d9-4522-8208-ba854654eb07");
	public class EarthSlayer extends ItemProxy { public EarthSlayer(String id) { super(id); } }
	public static EarthSlayer EarthSlayer = instance.new EarthSlayer("f98b4d8d-b45c-4023-b2b6-af524a3d99d4");
	public class WindSlayer extends ItemProxy { public WindSlayer(String id) { super(id); } }
	public static WindSlayer WindSlayer = instance.new WindSlayer("f7df3127-b48f-4c94-abe3-0dd275787720");
	public class WaterSlayer extends ItemProxy { public WaterSlayer(String id) { super(id); } }
	public static WaterSlayer WaterSlayer = instance.new WaterSlayer("723d3fbf-09d7-4723-b36b-ab2162c3aa02");
	public class KamiSlayer extends ItemProxy { public KamiSlayer(String id) { super(id); } }
	public static KamiSlayer KamiSlayer = instance.new KamiSlayer("111b5268-4153-4060-9d3e-20714c6a656c");
	public class Kodachi extends ItemProxy { public Kodachi(String id) { super(id); } }
	public static Kodachi Kodachi = instance.new Kodachi("dfabf1c9-6186-4d64-b6da-65413b1da561");
	public class SakuraKodachi extends ItemProxy { public SakuraKodachi(String id) { super(id); } }
	public static SakuraKodachi SakuraKodachi = instance.new SakuraKodachi("9303603d-f6e0-4bcc-a9f8-f3db6b431408");
	public class BloomingSakura extends ItemProxy { public BloomingSakura(String id) { super(id); } }
	public static BloomingSakura BloomingSakura = instance.new BloomingSakura("0481d559-ee80-414a-b29c-fa718f363aff");
	public class ViperJaw extends ItemProxy { public ViperJaw(String id) { super(id); } }
	public static ViperJaw ViperJaw = instance.new ViperJaw("f62faa7c-ca4b-403f-9618-ff117478e84c");
	public class BurningJaw extends ItemProxy { public BurningJaw(String id) { super(id); } }
	public static BurningJaw BurningJaw = instance.new BurningJaw("7f9d742d-80f1-46aa-ab34-b56a6ddfc1ec");
	public class SharkJaw extends ItemProxy { public SharkJaw(String id) { super(id); } }
	public static SharkJaw SharkJaw = instance.new SharkJaw("85c6a7b0-5998-4504-8150-bae6aebc912c");
	public class DrippingJaw extends ItemProxy { public DrippingJaw(String id) { super(id); } }
	public static DrippingJaw DrippingJaw = instance.new DrippingJaw("8c4bc08c-8e0d-44cd-a03b-0b76c7dcf73d");
	public class FineKodachi extends ItemProxy { public FineKodachi(String id) { super(id); } }
	public static FineKodachi FineKodachi = instance.new FineKodachi("a0f5204b-b992-42d3-8364-79f192d4643c");
	public class DualKodachi extends ItemProxy { public DualKodachi(String id) { super(id); } }
	public static DualKodachi DualKodachi = instance.new DualKodachi("ee6c70d0-2052-46ec-b44f-299e928500cf");
	public class DarkKodachi extends ItemProxy { public DarkKodachi(String id) { super(id); } }
	public static DarkKodachi DarkKodachi = instance.new DarkKodachi("f333805b-bfde-486a-b9fc-3c0e6601bf3e");
	public class DualDarkKodachi extends ItemProxy { public DualDarkKodachi(String id) { super(id); } }
	public static DualDarkKodachi DualDarkKodachi = instance.new DualDarkKodachi("56ccb891-d726-4217-96e1-bd6b005f14b2");
	public class SteelBranch extends ItemProxy { public SteelBranch(String id) { super(id); } }
	public static SteelBranch SteelBranch = instance.new SteelBranch("45f20c20-c063-4196-99be-efaf211ff48f");
	public class CarbonBranch extends ItemProxy { public CarbonBranch(String id) { super(id); } }
	public static CarbonBranch CarbonBranch = instance.new CarbonBranch("7dee261b-c96b-40c2-9d5f-bdf098e6dcfd");
	public class CursedBranch extends ItemProxy { public CursedBranch(String id) { super(id); } }
	public static CursedBranch CursedBranch = instance.new CursedBranch("e9ef86c0-6a36-4c1b-be1f-fc3aa62c1a5c");
	public class ThirstyBranch extends ItemProxy { public ThirstyBranch(String id) { super(id); } }
	public static ThirstyBranch ThirstyBranch = instance.new ThirstyBranch("79999914-2360-4e02-83fe-1e708d732965");
	public class GoldBranch extends ItemProxy { public GoldBranch(String id) { super(id); } }
	public static GoldBranch GoldBranch = instance.new GoldBranch("bac9b049-1406-423c-a768-7efc5124e371");
	public class AdamantBranch extends ItemProxy { public AdamantBranch(String id) { super(id); } }
	public static AdamantBranch AdamantBranch = instance.new AdamantBranch("c48b3f30-e75c-4268-8c9a-c5eb26075efc");
	public class DeitiesBlaze extends ItemProxy { public DeitiesBlaze(String id) { super(id); } }
	public static DeitiesBlaze DeitiesBlaze = instance.new DeitiesBlaze("d80fb941-6944-46ee-b93d-20a51146654d");
	public class DeitiesTsunami extends ItemProxy { public DeitiesTsunami(String id) { super(id); } }
	public static DeitiesTsunami DeitiesTsunami = instance.new DeitiesTsunami("9317a841-f1fd-476b-97a3-8a8b2bc840c5");
	public class DeitiesDarkness extends ItemProxy { public DeitiesDarkness(String id) { super(id); } }
	public static DeitiesDarkness DeitiesDarkness = instance.new DeitiesDarkness("2fb79a56-275e-4cfd-ba35-73201039c5da");
	public class SteelThistle extends ItemProxy { public SteelThistle(String id) { super(id); } }
	public static SteelThistle SteelThistle = instance.new SteelThistle("47648ed5-5a3b-4e5d-8780-eb31afd800ff");
	public class EtchedThistle extends ItemProxy { public EtchedThistle(String id) { super(id); } }
	public static EtchedThistle EtchedThistle = instance.new EtchedThistle("1573e6fe-b6a1-4d28-b3f7-4aa2fb4c6fc5");
	public class BuddingThistle extends ItemProxy { public BuddingThistle(String id) { super(id); } }
	public static BuddingThistle BuddingThistle = instance.new BuddingThistle("6cbc0720-9101-4c77-bbaf-e6d719f28cb2");
	public class CommonTanto extends ItemProxy { public CommonTanto(String id) { super(id); } }
	public static CommonTanto CommonTanto = instance.new CommonTanto("cb5f50b9-b806-4a56-bef2-97ceddaef4a9");
	public class DecoratedTanto extends ItemProxy { public DecoratedTanto(String id) { super(id); } }
	public static DecoratedTanto DecoratedTanto = instance.new DecoratedTanto("df561aaa-399e-44dc-987f-c873df06f65f");
	public class FineTanto extends ItemProxy { public FineTanto(String id) { super(id); } }
	public static FineTanto FineTanto = instance.new FineTanto("244bafe3-3dcf-47e4-a95f-cca218b8d76a");
	public class ForestTanto extends ItemProxy { public ForestTanto(String id) { super(id); } }
	public static ForestTanto ForestTanto = instance.new ForestTanto("00d061fe-78c2-4096-9589-60178426e68f");
	public class ShadowTanto extends ItemProxy { public ShadowTanto(String id) { super(id); } }
	public static ShadowTanto ShadowTanto = instance.new ShadowTanto("a3debeb0-9ab5-488e-a81f-18273a7ab4a5");
	public class ExquisiteTanto extends ItemProxy { public ExquisiteTanto(String id) { super(id); } }
	public static ExquisiteTanto ExquisiteTanto = instance.new ExquisiteTanto("9f86e943-d870-450f-bdc0-e0ca25972ddf");
	public class ShiningTanto extends ItemProxy { public ShiningTanto(String id) { super(id); } }
	public static ShiningTanto ShiningTanto = instance.new ShiningTanto("62fe97d1-dcef-48d9-a1cc-e5508b69a400");
	public class WhisperingTanto extends ItemProxy { public WhisperingTanto(String id) { super(id); } }
	public static WhisperingTanto WhisperingTanto = instance.new WhisperingTanto("77b3cd64-fa4b-41b8-a4c3-fca8e57fc20f");
	public class ShogunsTanto extends ItemProxy { public ShogunsTanto(String id) { super(id); } }
	public static ShogunsTanto ShogunsTanto = instance.new ShogunsTanto("668dbc8b-d4a5-4112-8304-1f9395a87f0d");
	public class EmperorsTanto extends ItemProxy { public EmperorsTanto(String id) { super(id); } }
	public static EmperorsTanto EmperorsTanto = instance.new EmperorsTanto("d4c73267-4540-4837-bf63-2dc577e1d65f");
	public class SteelNinjato extends ItemProxy { public SteelNinjato(String id) { super(id); } }
	public static SteelNinjato SteelNinjato = instance.new SteelNinjato("3457ac1a-6310-4978-a91f-fad25e250b92");
	public class BlessedNinjato extends ItemProxy { public BlessedNinjato(String id) { super(id); } }
	public static BlessedNinjato BlessedNinjato = instance.new BlessedNinjato("7fe59f88-8ebe-4be7-b7cb-2f7e6bbff6e0");
	public class CursedNinjato extends ItemProxy { public CursedNinjato(String id) { super(id); } }
	public static CursedNinjato CursedNinjato = instance.new CursedNinjato("5c022ed7-75ae-4c34-ad69-a38b0c395bf7");
	public class StudentsSteel extends ItemProxy { public StudentsSteel(String id) { super(id); } }
	public static StudentsSteel StudentsSteel = instance.new StudentsSteel("976a4d34-4dc7-4243-a6db-a85d5839826b");
	public class StudentsShadow extends ItemProxy { public StudentsShadow(String id) { super(id); } }
	public static StudentsShadow StudentsShadow = instance.new StudentsShadow("cd3cb1f5-ba7e-4c75-8aa7-eec32e2e90e5");
	public class MastersSteel extends ItemProxy { public MastersSteel(String id) { super(id); } }
	public static MastersSteel MastersSteel = instance.new MastersSteel("4f04c54f-68b2-4b55-968c-617798327c4a");
	public class MastersShadow extends ItemProxy { public MastersShadow(String id) { super(id); } }
	public static MastersShadow MastersShadow = instance.new MastersShadow("ba8791b8-d3ff-46fc-baf4-497f6b932254");
	public class KamisClaw extends ItemProxy { public KamisClaw(String id) { super(id); } }
	public static KamisClaw KamisClaw = instance.new KamisClaw("3931834e-82c6-401f-9011-d306618ad087");
	public class PossessedClaw extends ItemProxy { public PossessedClaw(String id) { super(id); } }
	public static PossessedClaw PossessedClaw = instance.new PossessedClaw("51e56395-4268-49ed-9d51-1457ccf4b7e3");
	public class KamisFang extends ItemProxy { public KamisFang(String id) { super(id); } }
	public static KamisFang KamisFang = instance.new KamisFang("a6edbb02-ac36-4790-a30b-41c8d9b7f931");
	public class AwakenedFang extends ItemProxy { public AwakenedFang(String id) { super(id); } }
	public static AwakenedFang AwakenedFang = instance.new AwakenedFang("44d8f14f-96cb-4e67-bcd4-f06c9e5f7e9f");
	public class Naginata extends ItemProxy { public Naginata(String id) { super(id); } }
	public static Naginata Naginata = instance.new Naginata("20399271-0e44-47b7-a57d-0dc797a40ab8");
	public class ExquisiteNaginata extends ItemProxy { public ExquisiteNaginata(String id) { super(id); } }
	public static ExquisiteNaginata ExquisiteNaginata = instance.new ExquisiteNaginata("ba1340a7-d2d2-4c01-a912-c8c0f38b14e8");
	public class BambooSpear extends ItemProxy { public BambooSpear(String id) { super(id); } }
	public static BambooSpear BambooSpear = instance.new BambooSpear("ab87ff59-3305-4db4-9481-96b53bee3e4a");
	public class VillagersSpear extends ItemProxy { public VillagersSpear(String id) { super(id); } }
	public static VillagersSpear VillagersSpear = instance.new VillagersSpear("aa72df98-ba94-473d-b0d4-0e120557d975");
	public class Yari extends ItemProxy { public Yari(String id) { super(id); } }
	public static Yari Yari = instance.new Yari("4d5c79bc-b176-42c4-ad1c-e67c584508fc");
	public class SteelFork extends ItemProxy { public SteelFork(String id) { super(id); } }
	public static SteelFork SteelFork = instance.new SteelFork("d8767289-6f7c-4ede-967e-b6ff19f0caa1");
	public class FineSteelFork extends ItemProxy { public FineSteelFork(String id) { super(id); } }
	public static FineSteelFork FineSteelFork = instance.new FineSteelFork("2c300300-76f9-4316-b5ad-ad9d6c6e11d9");
	public class GoldFork extends ItemProxy { public GoldFork(String id) { super(id); } }
	public static GoldFork GoldFork = instance.new GoldFork("b40ab5a5-6237-428d-9b69-f297fea3f361");
	public class DarkFork extends ItemProxy { public DarkFork(String id) { super(id); } }
	public static DarkFork DarkFork = instance.new DarkFork("6e6d5a64-1b93-49ab-8357-e1dcc0286590");
	public class GreatForestSpear extends ItemProxy { public GreatForestSpear(String id) { super(id); } }
	public static GreatForestSpear GreatForestSpear = instance.new GreatForestSpear("563bcc71-3daf-4907-b7a0-51eb3804f780");
	public class ForestElderSpear extends ItemProxy { public ForestElderSpear(String id) { super(id); } }
	public static ForestElderSpear ForestElderSpear = instance.new ForestElderSpear("df922e0e-29ce-40f8-b340-ac11ddbe3e44");
	public class SunsRay extends ItemProxy { public SunsRay(String id) { super(id); } }
	public static SunsRay SunsRay = instance.new SunsRay("47271b39-a85f-46a5-9113-7b119087187c");
	public class AwakenedRay extends ItemProxy { public AwakenedRay(String id) { super(id); } }
	public static AwakenedRay AwakenedRay = instance.new AwakenedRay("b08c7056-104d-43aa-8c20-3a2a775b8cc2");
	public class MoonsBeam extends ItemProxy { public MoonsBeam(String id) { super(id); } }
	public static MoonsBeam MoonsBeam = instance.new MoonsBeam("95b0af57-9ed0-43c2-ad2f-72378b875faf");
	public class AwakenedBeam extends ItemProxy { public AwakenedBeam(String id) { super(id); } }
	public static AwakenedBeam AwakenedBeam = instance.new AwakenedBeam("b795824c-87c4-4601-bdf3-2880ae618cbb");
	public class DivineBurst extends ItemProxy { public DivineBurst(String id) { super(id); } }
	public static DivineBurst DivineBurst = instance.new DivineBurst("a43ddc38-70ca-4ff0-bc58-4c02584361ad");
	public class BurstofFire extends ItemProxy { public BurstofFire(String id) { super(id); } }
	public static BurstofFire BurstofFire = instance.new BurstofFire("3bd10cbf-e3c1-467b-8a10-ea71bb8591e4");
	public class BurstofVoid extends ItemProxy { public BurstofVoid(String id) { super(id); } }
	public static BurstofVoid BurstofVoid = instance.new BurstofVoid("3c121163-250e-40a3-9609-ef8228e9bfd4");
	public class MasterYari extends ItemProxy { public MasterYari(String id) { super(id); } }
	public static MasterYari MasterYari = instance.new MasterYari("c8e360b5-a1ee-409f-a416-32eebb9b22e0");
	public class KazanYari extends ItemProxy { public KazanYari(String id) { super(id); } }
	public static KazanYari KazanYari = instance.new KazanYari("25cb2464-e80d-4b0d-9dc5-a7e341f46699");
	public class ShiningFork extends ItemProxy { public ShiningFork(String id) { super(id); } }
	public static ShiningFork ShiningFork = instance.new ShiningFork("61f3b5c9-294a-4bd6-a888-d16972b756be");
	public class BlazingFork extends ItemProxy { public BlazingFork(String id) { super(id); } }
	public static BlazingFork BlazingFork = instance.new BlazingFork("b39b75c2-ba30-4952-a71d-ebd1ed52667f");
	public class VoidFork extends ItemProxy { public VoidFork(String id) { super(id); } }
	public static VoidFork VoidFork = instance.new VoidFork("fc34c145-b761-4c1d-be17-53c0acfe3ad6");
	public class DomantLightning extends ItemProxy { public DomantLightning(String id) { super(id); } }
	public static DomantLightning DomantLightning = instance.new DomantLightning("66cc3dda-8e74-4210-94c9-91f6e6d4d7dc");
	public class LightningsFury extends ItemProxy { public LightningsFury(String id) { super(id); } }
	public static LightningsFury LightningsFury = instance.new LightningsFury("18340d26-a2a3-4efd-8169-7decef749b60");
	public class DormantAvalanche extends ItemProxy { public DormantAvalanche(String id) { super(id); } }
	public static DormantAvalanche DormantAvalanche = instance.new DormantAvalanche("8e8a8341-0894-4c62-9a1e-e6978b138979");
	public class AvalanchesFury extends ItemProxy { public AvalanchesFury(String id) { super(id); } }
	public static AvalanchesFury AvalanchesFury = instance.new AvalanchesFury("50a8e5e7-56a1-45e5-9e7c-2531da473766");
	public class SteelBolts extends ItemProxy { public SteelBolts(String id) { super(id); } }
	public static SteelBolts SteelBolts = instance.new SteelBolts("50a342b1-01d8-443a-9975-c3a2843bc82f");
	public class CarbideBolts extends ItemProxy { public CarbideBolts(String id) { super(id); } }
	public static CarbideBolts CarbideBolts = instance.new CarbideBolts("e79ad3fa-82c1-47a0-bea4-49d6e3dc5657");
	public class ThrowingKnifes extends ItemProxy { public ThrowingKnifes(String id) { super(id); } }
	public static ThrowingKnifes ThrowingKnifes = instance.new ThrowingKnifes("b1ef2993-518d-45e9-a61c-fbb12db2911e");
	public class FineKnifes extends ItemProxy { public FineKnifes(String id) { super(id); } }
	public static FineKnifes FineKnifes = instance.new FineKnifes("b8b48063-1d27-4c6d-a03c-3449fad6bfff");
	public class ThrowingDaggers extends ItemProxy { public ThrowingDaggers(String id) { super(id); } }
	public static ThrowingDaggers ThrowingDaggers = instance.new ThrowingDaggers("c25bc03e-e2b3-4c5e-aafc-74f48d959c69");
	public class MoltenBolts extends ItemProxy { public MoltenBolts(String id) { super(id); } }
	public static MoltenBolts MoltenBolts = instance.new MoltenBolts("b2924f50-3992-44bb-93f3-3c7fc0aa5c3f");
	public class IronKunai extends ItemProxy { public IronKunai(String id) { super(id); } }
	public static IronKunai IronKunai = instance.new IronKunai("3b8e768b-0b0a-4206-8fbc-0cb76d3d98dd");
	public class CarbideKunai extends ItemProxy { public CarbideKunai(String id) { super(id); } }
	public static CarbideKunai CarbideKunai = instance.new CarbideKunai("7ffdc4f3-da25-4427-815c-c21aef7c4888");
	public class IgasEarth extends ItemProxy { public IgasEarth(String id) { super(id); } }
	public static IgasEarth IgasEarth = instance.new IgasEarth("47cb52cf-d0a9-4252-b5eb-aa7fea4ce91e");
	public class IgasRemorse extends ItemProxy { public IgasRemorse(String id) { super(id); } }
	public static IgasRemorse IgasRemorse = instance.new IgasRemorse("fbc657ba-2eda-4298-b2b4-175e3f9359c0");
	public class IgasFreedom extends ItemProxy { public IgasFreedom(String id) { super(id); } }
	public static IgasFreedom IgasFreedom = instance.new IgasFreedom("aa5afa7c-0a5e-410f-a169-7dde3e6c36a2");
	public class LapazKunai extends ItemProxy { public LapazKunai(String id) { super(id); } }
	public static LapazKunai LapazKunai = instance.new LapazKunai("e015961f-2f82-4580-a37c-62f76c9df60f");
	public class ImbuedLapaz extends ItemProxy { public ImbuedLapaz(String id) { super(id); } }
	public static ImbuedLapaz ImbuedLapaz = instance.new ImbuedLapaz("e9d7d674-a6e9-483d-b287-6b2930711559");
	public class AmethystKunai extends ItemProxy { public AmethystKunai(String id) { super(id); } }
	public static AmethystKunai AmethystKunai = instance.new AmethystKunai("34773cb2-f1e9-4be6-9128-d9265bb8be12");
	public class ImbuedAmethyst extends ItemProxy { public ImbuedAmethyst(String id) { super(id); } }
	public static ImbuedAmethyst ImbuedAmethyst = instance.new ImbuedAmethyst("b9296656-cc85-4ef3-b20a-0ecf22b6aab4");
	public class DiamondKunai extends ItemProxy { public DiamondKunai(String id) { super(id); } }
	public static DiamondKunai DiamondKunai = instance.new DiamondKunai("42880b82-8493-4fdb-b480-b90e5e7bc6f6");
	public class ImbuedDiamond extends ItemProxy { public ImbuedDiamond(String id) { super(id); } }
	public static ImbuedDiamond ImbuedDiamond = instance.new ImbuedDiamond("c465d575-d34b-4ff5-a676-ad71e84a3b18");
	public class Kama extends ItemProxy { public Kama(String id) { super(id); } }
	public static Kama Kama = instance.new Kama("e3682845-edc5-4686-9e3c-7afc591b9080");
	public class Kusarigama extends ItemProxy { public Kusarigama(String id) { super(id); } }
	public static Kusarigama Kusarigama = instance.new Kusarigama("5514ece3-4684-4b00-b9fa-4d069e0a6ac8");
	public class YokaisVengence extends ItemProxy { public YokaisVengence(String id) { super(id); } }
	public static YokaisVengence YokaisVengence = instance.new YokaisVengence("8ba8464b-83ed-4a00-912b-157b8a0c1726");
	public class SpiritSeal extends ItemProxy { public SpiritSeal(String id) { super(id); } }
	public static SpiritSeal SpiritSeal = instance.new SpiritSeal("f7d1813a-db31-4883-90fd-984abf9720db");
	public class FireSeal extends ItemProxy { public FireSeal(String id) { super(id); } }
	public static FireSeal FireSeal = instance.new FireSeal("a193ce58-a336-4b33-9a9b-91a32a383fad");
	public class WaterSeal extends ItemProxy { public WaterSeal(String id) { super(id); } }
	public static WaterSeal WaterSeal = instance.new WaterSeal("9dc493a4-8b11-4abe-83ae-0d9ca8854aa5");
	public class LightSeal extends ItemProxy { public LightSeal(String id) { super(id); } }
	public static LightSeal LightSeal = instance.new LightSeal("60677577-bd39-4859-b112-d9e2808a5a34");
	public class BanishingSeal extends ItemProxy { public BanishingSeal(String id) { super(id); } }
	public static BanishingSeal BanishingSeal = instance.new BanishingSeal("754fecca-d844-4357-a4da-5d2c18f79f0e");
	public class DualismSeal extends ItemProxy { public DualismSeal(String id) { super(id); } }
	public static DualismSeal DualismSeal = instance.new DualismSeal("da4134c9-598b-4e33-a058-e514b515a221");
	public class UnholySeal extends ItemProxy { public UnholySeal(String id) { super(id); } }
	public static UnholySeal UnholySeal = instance.new UnholySeal("5a38ce5a-ee23-4135-8fd2-7b6897325ec1");
	public class CheapYumi extends ItemProxy { public CheapYumi(String id) { super(id); } }
	public static CheapYumi CheapYumi = instance.new CheapYumi("176bd828-8d92-4a3d-a0e0-2ecbd43a867c");
	public class RestoredYumi extends ItemProxy { public RestoredYumi(String id) { super(id); } }
	public static RestoredYumi RestoredYumi = instance.new RestoredYumi("c81b57c0-d3d3-4437-933e-f8b93f887b4f");
	public class BattleBow extends ItemProxy { public BattleBow(String id) { super(id); } }
	public static BattleBow BattleBow = instance.new BattleBow("7c051049-cec5-4ba4-a640-c060c706dfbb");
	public class SakuraGreatbow extends ItemProxy { public SakuraGreatbow(String id) { super(id); } }
	public static SakuraGreatbow SakuraGreatbow = instance.new SakuraGreatbow("c3d2d294-a8c3-4c1b-a55f-7f09d3098c8b");
	public class KamisFeather extends ItemProxy { public KamisFeather(String id) { super(id); } }
	public static KamisFeather KamisFeather = instance.new KamisFeather("6bcbb4f0-e3b5-4734-a70d-596b2ec39ecc");
	public class WaterDragonBow extends ItemProxy { public WaterDragonBow(String id) { super(id); } }
	public static WaterDragonBow WaterDragonBow = instance.new WaterDragonBow("2071912b-8d04-4b5b-8410-3eadeed9acf3");
	public class FireDragonBow extends ItemProxy { public FireDragonBow(String id) { super(id); } }
	public static FireDragonBow FireDragonBow = instance.new FireDragonBow("826ac08a-4b79-41dd-a2c5-40ca683b4dd6");
	public class FlintlockRifle extends ItemProxy { public FlintlockRifle(String id) { super(id); } }
	public static FlintlockRifle FlintlockRifle = instance.new FlintlockRifle("3f50e525-e366-49d7-97db-db5035ec218a");
	public class WornShotgun extends ItemProxy { public WornShotgun(String id) { super(id); } }
	public static WornShotgun WornShotgun = instance.new WornShotgun("b6cc0a91-6776-479b-8571-740941a0bb20");
	public class FineShotgun extends ItemProxy { public FineShotgun(String id) { super(id); } }
	public static FineShotgun FineShotgun = instance.new FineShotgun("6f985d51-60e9-47b5-b9f2-c293e6b70f60");
	public class MasterShotgun extends ItemProxy { public MasterShotgun(String id) { super(id); } }
	public static MasterShotgun MasterShotgun = instance.new MasterShotgun("25718550-b39a-4239-b0d9-adbbd54f9b8d");
	public class HeavyShotgun extends ItemProxy { public HeavyShotgun(String id) { super(id); } }
	public static HeavyShotgun HeavyShotgun = instance.new HeavyShotgun("efa19c9e-79c8-4071-90f1-247b9eb95bf0");
	public class GatlingGun extends ItemProxy { public GatlingGun(String id) { super(id); } }
	public static GatlingGun GatlingGun = instance.new GatlingGun("ea50e4c9-9b4f-4fe7-ba13-b259ed5bfd79");
	public class CursedGatling extends ItemProxy { public CursedGatling(String id) { super(id); } }
	public static CursedGatling CursedGatling = instance.new CursedGatling("e01e32a9-6b00-4450-a878-b8ed194c57f5");
	public class SecretTechMk1 extends ItemProxy { public SecretTechMk1(String id) { super(id); } }
	public static SecretTechMk1 SecretTechMk1 = instance.new SecretTechMk1("e219da49-1585-496b-ab62-60b32ec1cf2e");
	public class SecretTechMk2 extends ItemProxy { public SecretTechMk2(String id) { super(id); } }
	public static SecretTechMk2 SecretTechMk2 = instance.new SecretTechMk2("2d01afec-ccd4-4016-a938-599ba1e40bb5");
	public class SteelShuiken extends ItemProxy { public SteelShuiken(String id) { super(id); } }
	public static SteelShuiken SteelShuiken = instance.new SteelShuiken("e4a82c4f-3a5a-4c17-a53a-1fe14843e172");
	public class SteelWindmill extends ItemProxy { public SteelWindmill(String id) { super(id); } }
	public static SteelWindmill SteelWindmill = instance.new SteelWindmill("402fbb23-4423-4fe9-8773-d7a8972f66f7");
	public class CarbideWindmill extends ItemProxy { public CarbideWindmill(String id) { super(id); } }
	public static CarbideWindmill CarbideWindmill = instance.new CarbideWindmill("4669e869-78ae-4a71-8ef3-e0184476fb44");
	public class KodachiWindmill extends ItemProxy { public KodachiWindmill(String id) { super(id); } }
	public static KodachiWindmill KodachiWindmill = instance.new KodachiWindmill("b202ac3f-b467-49ed-ad7a-7486324f63b9");
	public class SteelCross extends ItemProxy { public SteelCross(String id) { super(id); } }
	public static SteelCross SteelCross = instance.new SteelCross("6ed10a5c-d719-4cec-8170-cf97d9358777");
	public class CarbideCross extends ItemProxy { public CarbideCross(String id) { super(id); } }
	public static CarbideCross CarbideCross = instance.new CarbideCross("d808af89-b9ba-440b-9421-7bced896dc7c");
	public class GoldCross extends ItemProxy { public GoldCross(String id) { super(id); } }
	public static GoldCross GoldCross = instance.new GoldCross("a07db3d4-d858-4e4c-bd3e-6d6a411be410");
	public class YokaisCross extends ItemProxy { public YokaisCross(String id) { super(id); } }
	public static YokaisCross YokaisCross = instance.new YokaisCross("f3765a14-c15c-4c5d-a103-d480b075b74e");
	public class Makibishi extends ItemProxy { public Makibishi(String id) { super(id); } }
	public static Makibishi Makibishi = instance.new Makibishi("8592e5e2-51cd-499c-bbd3-440d139c5b68");
	public class SmokeBomb extends ItemProxy { public SmokeBomb(String id) { super(id); } }
	public static SmokeBomb SmokeBomb = instance.new SmokeBomb("16a4c893-0acb-444a-8f75-be581b8b5207");
	public class FlashBomb extends ItemProxy { public FlashBomb(String id) { super(id); } }
	public static FlashBomb FlashBomb = instance.new FlashBomb("fe24a01f-724f-4fb5-b8a0-17b777ab2506");
	public class ShrapnelBomb extends ItemProxy { public ShrapnelBomb(String id) { super(id); } }
	public static ShrapnelBomb ShrapnelBomb = instance.new ShrapnelBomb("83918cf8-70f1-4ae5-9ab4-f6e8fadc262e");
	
	
	// Weapon Items ->
}
