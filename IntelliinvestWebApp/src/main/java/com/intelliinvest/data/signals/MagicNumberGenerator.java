package com.intelliinvest.data.signals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.intelliinvest.common.IntelliinvestConstants;
import com.intelliinvest.data.dao.MagicNumberRepository;
import com.intelliinvest.data.dao.QuandlEODStockPriceRepository;
import com.intelliinvest.data.dao.StockRepository;
import com.intelliinvest.data.model.MagicNumberData;
import com.intelliinvest.data.model.QuandlStockPrice;
import com.intelliinvest.data.model.Stock;
import com.intelliinvest.data.model.StockSignalsDTO;
import com.intelliinvest.util.HttpUtil;

public class MagicNumberGenerator {

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private QuandlEODStockPriceRepository quandlEODStockPriceRepository;

	@Autowired
	private MagicNumberRepository magicNumberRepository;

	@Autowired
	private StockSignalsGenerator stockSignalsGenerator;

	private static Logger logger = Logger.getLogger(MagicNumberGenerator.class);

	static String[] stocks = new String[] { "20MICRONS", "3IINFOTECH", "3MINDIA", "8KMILES", "A2ZINFRA", "AARTIDRUGS",
			"AARTIIND", "AARVEEDEN", "ABAN", "ABB", "ABBOTINDIA", "ABFRL", "ABGSHIP", "ABIRLANUVO", "ACC", "ACCELYA",
			"ACE", "ADANIENT", "ADANIPORTS", "ADANIPOWER", "ADANITRANS", "ADFFOODS", "ADHUNIK", "ADHUNIKIND", "ADI",
			"ADLABS", "ADORWELD", "ADSL", "ADVANIHOTR", "ADVENZYMES", "AEGISCHEM", "AFL", "AGARIND", "AGCNET",
			"AGRITECH", "AGRODUTCH", "AHLEAST", "AHLUCONT", "AHLWEST", "AIAENG", "AIFL", "AIL", "AJANTPHARM", "AJMERA",
			"AKSHOPTFBR", "AKZOINDIA", "ALANKIT", "ALBERTDAVD", "ALBK", "ALCHEM", "ALEMBICLTD", "ALICON", "ALKALI",
			"ALKEM", "ALKYLAMINE", "ALLCARGO", "ALLSEC", "ALMONDZ", "ALOKTEXT", "ALPA", "ALPHAGEO", "ALPSINDUS",
			"ALSTOMT&D", "AMARAJABAT", "AMBIKCO", "AMBUJACEM", "AMDIND", "AMRUTANJAN", "AMTEKAUTO", "AMTL", "ANANTRAJ",
			"ANDHRABANK", "ANDHRACEMT", "ANDHRSUGAR", "ANGIND", "ANIKINDS", "ANKITMETAL", "ANSALAPI", "ANSALHSG",
			"ANTGRAPHIC", "APARINDS", "APCOTEXIND", "APLAPOLLO", "APLLTD", "APOLLOHOSP", "APOLLOTYRE", "APOLSINHOT",
			"APTECHT", "ARCHIDPLY", "ARCHIES", "ARCOTECH", "ARIES", "ARIHANT", "ARMANFIN", "AROGRANITE", "ARROWGREEN",
			"ARROWTEX", "ARSHIYA", "ARSSINFRA", "ARVIND", "ARVINFRA", "ASAHIINDIA", "ASAHISONG", "ASAL", "ASHAPURMIN",
			"ASHIANA", "ASHIMASYN", "ASHOKA", "ASHOKLEY", "ASIANHOTNR", "ASIANPAINT", "ASIANTILES", "ASIL", "ASPINWALL",
			"ASSAMCO", "ASTEC", "ASTRAL", "ASTRAMICRO", "ASTRAZEN", "ATFL", "ATLANTA", "ATLASCYCLE", "ATUL", "ATULAUTO",
			"AURIONPRO", "AUROPHARMA", "AUSOMENT", "AUSTRAL", "AUTOAXLES", "AUTOIND", "AUTOLITIND", "AVANTIFEED",
			"AVTNPL", "AXISBANK", "AXISCADES", "AXISGOLD", "AYMSYNTEX", "BAFNAPHARM", "BAGFILMS", "BAJAJ-AUTO",
			"BAJAJCORP", "BAJAJELEC", "BAJAJFINSV", "BAJAJHIND", "BAJAJHLDNG", "BAJFINANCE", "BALAJITELE", "BALAMINES",
			"BALKRISHNA", "BALKRISIND", "BALLARPUR", "BALMLAWRIE", "BALPHARMA", "BALRAMCHIN", "BANARBEADS", "BANARISUG",
			"BANCOINDIA", "BANG", "BANKBARODA", "BANKBEES", "BANKINDIA", "BANSWRAS", "BARTRONICS", "BASF", "BASML",
			"BATAINDIA", "BAYERCROP", "BBL", "BBTC", "BEARDSELL", "BEDMUTHA", "BEL", "BEML", "BEPL", "BERGEPAINT",
			"BFINVEST", "BFUTILITIE", "BGLOBAL", "BGRENERGY", "BHAGERIA", "BHAGYNAGAR", "BHARATFIN", "BHARATFORG",
			"BHARATGEAR", "BHARATIDIL", "BHARATRAS", "BHARATWIRE", "BHARTIARTL", "BHEL", "BHUSANSTL", "BIGBLOC", "BIL",
			"BILENERGY", "BILPOWER", "BINANIIND", "BINDALAGRO", "BIOCON", "BIRLACORPN", "BIRLACOT", "BIRLAERIC",
			"BIRLAMONEY", "BLBLIMITED", "BLISSGVS", "BLKASHYAP", "BLS", "BLUEBLENDS", "BLUEDART", "BLUEDART",
			"BLUESTARCO", "BODALCHEM", "BOMDYEING", "BOSCHLTD", "BPCL", "BPL", "BRFL", "BRIGADE", "BRITANNIA", "BROOKS",
			"BSELINFRA", "BSL", "BSLGOLDETF", "BSLIMITED", "BSLNIFTY", "BURNPUR", "BUTTERFLY", "BVCL", "BYKE",
			"CADILAHC", "CAIRN", "CAMLINFINE", "CANBK", "CANDC", "CANFINHOME", "CANTABIL", "CAPF", "CAPLIPOINT",
			"CARBORUNIV", "CAREERP", "CARERATING", "CASTEXTECH", "CASTROLIND", "CCCL", "CCHHL", "CCL", "CEATLTD",
			"CEBBCO", "CELEBRITY", "CELESTIAL", "CENTENKA", "CENTEXT", "CENTRALBK", "CENTUM", "CENTURYPLY",
			"CENTURYTEX", "CERA", "CEREBRAINT", "CESC", "CGCL", "CHAMBLFERT", "CHEMFALKAL", "CHENNPETRO", "CHOLAFIN",
			"CHROMATIC", "CIGNITITEC", "CIMMCO", "CINELINE", "CINEVISTA", "CIPLA", "CLNINDIA", "COALINDIA", "COFFEEDAY",
			"COLPAL", "COMPINFO", "COMPUSOFT", "CONCOR", "CONSOFINVT", "CONTROLPR", "CORDSCABLE", "COROMANDEL",
			"CORPBANK", "COSMOFILMS", "COUNCODOS", "COX&KINGS", "CPSEETF", "CREATIVEYE", "CREST", "CRISIL", "CRMFGETF",
			"CROMPGREAV", "CROMPTON", "CTE", "CUB", "CUMMINSIND", "CURATECH", "CYBERTECH", "CYIENT", "DAAWAT", "DABUR",
			"DALMIABHA", "DALMIASUG", "DAMODARIND", "DATAMATICS", "DBCORP", "DBL", "DBREALTY", "DCBBANK", "DCM",
			"DCMSHRIRAM", "DCW", "DECCANCE", "DEEPAKFERT", "DEEPAKNTR", "DEEPIND", "DELTACORP", "DEN", "DENABANK",
			"DENORA", "DHAMPURSUG", "DHANBANK", "DHANUKA", "DHARSUGAR", "DHFL", "DHFL", "DHFL", "DHFL", "DHFL", "DHFL",
			"DHFL", "DHUNINV", "DIAPOWER", "DICIND", "DIGJAMLTD", "DISHMAN", "DISHTV", "DIVISLAB", "DLF", "DLINKINDIA",
			"DOLPHINOFF", "DONEAR", "DPL", "DPSCLTD", "DQE", "DREDGECORP", "DRREDDY", "DSKULKARNI", "DSSL", "DTIL",
			"DUCON", "DWARKESH", "DYNAMATECH", "EASTSILK", "EASUNREYRL", "ECEIND", "ECLERX", "ECLFINANCE", "ECLFINANCE",
			"ECLFINANCE", "ECLFINANCE", "ECLFINANCE", "EDELWEISS", "EDL", "EDUCOMP", "EHFLNCD", "EHFLNCD", "EHFLNCD",
			"EHFLNCD", "EICHERMOT", "EIDPARRY", "EIHAHOTELS", "EIHOTEL", "EIMCOELECO", "EKC", "ELAND", "ELDERPHARM",
			"ELECON", "ELECTCAST", "ELECTHERM", "ELGIEQUIP", "ELGIRUBCO", "EMAMIINFRA", "EMAMILTD", "EMCO", "EMKAY",
			"EMMBI", "ENERGYDEV", "ENGINERSIN", "ENIL", "EON", "EQ30", "EQUITAS", "ERAINFRA", "EROSMEDIA", "ESABINDIA",
			"ESCORTS", "ESL", "ESSARSHPNG", "ESSDEE", "ESSELPACK", "ESTER", "EUROCERA", "EUROTEXIND", "EVEREADY",
			"EVERESTIND", "EVERONN", "EXCEL", "EXCELCROP", "EXCELINDUS", "EXIDEIND", "FACT", "FAGBEARING", "FARMAXIND",
			"FCEL", "FCL", "FCSSOFT", "FDC", "FEDDERLOYD", "FEDERALBNK", "FEL", "FELDVR", "FIEMIND", "FILATEX",
			"FINANTECH", "FINCABLES", "FINPIPE", "FLEXITUFF", "FLFL", "FMGOETZE", "FMNL", "FORTIS", "FOSECOIND",
			"FOURTHDIM", "FRETAIL", "FSL", "GABRIEL", "GAEL", "GAIL", "GAL", "GALLANTT", "GALLISPAT", "GAMMNINFRA",
			"GAMMONIND", "GANDHITUBE", "GANECOS", "GANESHHOUC", "GARDENSILK", "GARWALLROP", "GATI", "GAYAPROJ",
			"GBNOV23", "GDL", "GEECEE", "GEINDSYS", "GEMINI", "GENESYS", "GENUSPAPER", "GENUSPOWER", "GEOJITBNPP",
			"GEOMETRIC", "GESHIP", "GHCL", "GICHSGFIN", "GILLANDERS", "GILLETTE", "GINNIFILA", "GIPCL", "GIRRESORTS",
			"GITANJALI", "GKWLIMITED", "GLAXO", "GLENMARK", "GLOBALVECT", "GLOBOFFS", "GLOBUSSPR", "GMBREW", "GMDCLTD",
			"GMRINFRA", "GNFC", "GOACARBON", "GOCLCORP", "GODFRYPHLP", "GODREJCP", "GODREJIND", "GODREJPROP", "GOENKA",
			"GOKEX", "GOKUL", "GOKULAGRO", "GOLDBEES", "GOLDENTOBC", "GOLDIAM", "GOLDINFRA", "GOLDIWIN", "GOLDSHARE",
			"GOLDTECH", "GOODLUCK", "GPIL", "GPPL", "GPTINFRA", "GRANULES", "GRAPHITE", "GRASIM", "GRAVITA",
			"GREAVESCOT", "GREENFIRE", "GREENLAM", "GREENPLY", "GREENPOWER", "GRINDWELL", "GROBTEA", "GRPLTD", "GRUH",
			"GSCLCEMENT", "GSFC", "GSKCONS", "GSPL", "GSS", "GTL", "GTLINFRA", "GTNIND", "GTNTEX", "GTOFFSHORE",
			"GUFICBIO", "GUJALKALI", "GUJAPOLLO", "GUJFLUORO", "GUJGASLTD", "GUJNRECOKE", "GUJNREDVR", "GULFOILLUB",
			"GULFPETRO", "GULPOLY", "GVKPIL", "HANUNG", "HARITASEAT", "HARRMALAYA", "HATHWAY", "HATSUN", "HAVELLS",
			"HBLPOWER", "HBSTOCK", "HCC", "HCG", "HCL-INSYS", "HCLTECH", "HDFC", "HDFC", "HDFCBANK", "HDFCMFGETF",
			"HDFCNIFETF", "HDIL", "HEG", "HEIDELBERG", "HERCULES", "HERITGFOOD", "HEROMOTOCO", "HESTERBIO",
			"HEXATRADEX", "HEXAWARE", "HFCL", "HGS", "HIKAL", "HIL", "HILTON", "HIMATSEIDE", "HINDALCO", "HINDCOMPOS",
			"HINDCOPPER", "HINDDORROL", "HINDMOTORS", "HINDNATGLS", "HINDOILEXP", "HINDPETRO", "HINDSYNTEX",
			"HINDUJAFO", "HINDUJAVEN", "HINDUNILVR", "HINDZINC", "HIRECT", "HITECH", "HITECHGEAR", "HITECHPLAS", "HMT",
			"HMVL", "HNGSNGBEES", "HOCL", "HONAUT", "HONDAPOWER", "HOTELEELA", "HOTELRUGBY", "HOVS", "HSCL", "HSIL",
			"HTMEDIA", "HUBTOWN", "HUDCO", "HUDCO", "HUDCO", "HUDCO", "IBREALEST", "IBULHSGFIN", "IBVENTURES", "IBWSL",
			"ICICIBANK", "ICIL", "ICRA", "ICSA", "IDBI", "IDBIGOLD", "IDEA", "IDFC", "IDFCBANK", "IDFCBANK", "IDFCBANK",
			"IDFCBANK", "IDFCBANK", "IDFCBANK", "IDFCBANK", "IFBAGRO", "IFBIND", "IFCI", "IFCI", "IFCI", "IFCI",
			"IFGLREFRAC", "IGARASHI", "IGL", "IGPL", "IIFL", "IIFLFIN", "IIFLFIN", "IIFLFIN", "IIFLFIN", "IIFLFIN",
			"IIFLFIN", "IIHFL", "IIHFL", "IIHFL", "IITL", "IL&FSENGG", "IL&FSTRANS", "IMFA", "IMPAL", "IMPEXFERRO",
			"INDBANK", "INDHOTEL", "INDIACEM", "INDIAGLYCO", "INDIANB", "INDIANCARD", "INDIANHUME", "INDIGO",
			"INDLMETER", "INDNIPPON", "INDOCO", "INDORAMA", "INDOSOLAR", "INDOTECH", "INDOTHAI", "INDOWIND",
			"INDRAMEDCO", "INDSWFTLAB", "INDSWFTLTD", "INDTERRAIN", "INDUSINDBK", "INEOSSTYRO", "INFIBEAM", "INFINITE",
			"INFOMEDIA", "INFRABEES", "INFRATEL", "INFY", "INGERRAND", "INOXLEISUR", "INOXWIND", "INSECTICID",
			"INTEGRA", "INTELLECT", "INVENTURE", "IOB", "IOC", "IOLCP", "IPAPPM", "IPCALAB", "IRB", "IREDA", "IRFC",
			"IRFC", "IRFC", "IRFC", "IRFC", "IRFC", "IRFC", "IRFC", "ISFT", "ISMTLTD", "ITC", "ITDCEM", "ITI", "IVC",
			"IVP", "IVRCLINFRA", "IVZINGOLD", "IVZINNIFTY", "IZMO", "J&KBANK", "JAGRAN", "JAGSNPHARM", "JAIBALAJI",
			"JAICORPLTD", "JAIHINDPRO", "JAINSTUDIO", "JAMNAAUTO", "JAYAGROGN", "JAYBARMARU", "JAYNECOIND",
			"JAYSREETEA", "JBCHEPHARM", "JBFIND", "JBMA", "JCHAC", "JENSONICOL", "JETAIRWAYS", "JHS", "JIKIND",
			"JINDALPHOT", "JINDALPOLY", "JINDALSAW", "JINDALSTEL", "JINDCOT", "JINDRILL", "JINDWORLD", "JISLDVREQS",
			"JISLJALEQS", "JKCEMENT", "JKIL", "JKLAKSHMI", "JKPAPER", "JKTYRE", "JMA", "JMCPROJECT", "JMFINANCIL",
			"JMTAUTOLTD", "JOCIL", "JPASSOCIAT", "JPINFRATEC", "JPOLYINVST", "JPPOWER", "JSL", "JSLHISAR", "JSWENERGY",
			"JSWHL", "JSWSTEEL", "JSWSTEEL", "JUBILANT", "JUBLFOOD", "JUBLINDS", "JUNIORBEES", "JUSTDIAL", "JVLAGRO",
			"JYOTHYLAB", "JYOTISTRUC", "KABRAEXTRU", "KAJARIACER", "KAKATCEM", "KALINDEE", "KALPATPOWR", "KALYANIFRG",
			"KAMATHOTEL", "KAMDHENU", "KANANIIND", "KANORICHEM", "KANSAINER", "KARMAENG", "KARURVYSYA", "KAUSHALYA",
			"KAVVERITEL", "KAYA", "KCP", "KCPSUGIND", "KDDL", "KEC", "KECL", "KEI", "KELLTONTEC", "KERNEX", "KESARENT",
			"KESORAMIND", "KGL", "KHAITANELE", "KHAITANLTD", "KHANDSE", "KICL", "KILITCH", "KIRIINDUS", "KIRLOSBROS",
			"KIRLOSENG", "KIRLOSIND", "KITEX", "KKCL", "KMSUGAR", "KNRCON", "KOHINOOR", "KOKUYOCMLN", "KOLTEPATIL",
			"KOPRAN", "KOTAKBANK", "KOTAKBKETF", "KOTAKGOLD", "KOTAKNIFTY", "KOTAKNV20", "KOTAKPSUBK", "KOTARISUG",
			"KOTHARIPET", "KOTHARIPRO", "KPIT", "KPRMILL", "KRBL", "KRIDHANINF", "KSBPUMPS", "KSCL", "KSERASERA", "KSK",
			"KSL", "KTIL", "KTKBANK", "KWALITY", "L&TFH", "L&TFINANCE", "L&TFINANCE", "L&TINFRA", "L&TINFRA",
			"L&TINFRA", "L&TINFRA", "L&TINFRA", "L&TINFRA", "LAKPRE", "LAKSHMIEFL", "LAKSHVILAS", "LALPATHLAB",
			"LAMBODHARA", "LAOPALA", "LAXMIMACH", "LCCINFOTEC", "LFIC", "LGBBROSLTD", "LGBFORGE", "LIBERTSHOE",
			"LICHSGFIN", "LICNETFGSC", "LICNETFN50", "LINCOLN", "LINCPEN", "LINDEINDIA", "LIQUIDBEES", "LITL",
			"LLOYDELENG", "LML", "LOKESHMACH", "LOTUSEYE", "LOVABLE", "LPDC", "LSIL", "LT", "LTI", "LUMAXAUTO",
			"LUMAXIND", "LUMAXTECH", "LUPIN", "LUXIND", "LYCOS", "LYKALABS", "LYPSAGEMS", "M&M", "M&MFIN", "M100",
			"M50", "MAANALU", "MADHAV", "MADHUCON", "MADRASFERT", "MAGMA", "MAGNUM", "MAHABANK", "MAHASTEEL",
			"MAHINDCIE", "MAHLIFE", "MAHSCOOTER", "MAHSEAMLES", "MAITHANALL", "MAJESCO", "MALUPAPER", "MALWACOTT",
			"MANAKALUCO", "MANAKCOAT", "MANAKINDST", "MANAKSIA", "MANAKSTEEL", "MANALIPETC", "MANAPPURAM", "MANAPPURAM",
			"MANDHANA", "MANGALAM", "MANGCHEFER", "MANGLMCEM", "MANGTIMBER", "MANINDS", "MANINFRA", "MANPASAND",
			"MANUGRAPH", "MARALOVER", "MARICO", "MARKSANS", "MARUTI", "MASTEK", "MAWANASUG", "MAXINDIA", "MAXVIL",
			"MAXWELL", "MAYURUNIQ", "MBAPL", "MBECL", "MBLINFRA", "MCDHOLDING", "MCDOWELL-N", "MCLEODRUSS", "MCX",
			"MEGASOFT", "MEGH", "MENONBE", "MEP", "MERCATOR", "MERCK", "METALFORGE", "METKORE", "MFSL", "MGL", "MHRIL",
			"MIC", "MICROSEC", "MIDCAPIWIN", "MINDACORP", "MINDAIND", "MINDTECK", "MINDTREE", "MIRCELECTR", "MIRZAINT",
			"MITCON", "MMFL", "MMTC", "MOHITIND", "MOIL", "MOLDTECH", "MOLDTKPAC", "MOMAI", "MONNETISPA", "MONSANTO",
			"MONTECARLO", "MORARJEE", "MOREPENLAB", "MOSERBAER", "MOTHERSUMI", "MOTILALOFS", "MOTOGENFIN", "MPHASIS",
			"MPSLTD", "MRF", "MRO-TEK", "MRPL", "MSPL", "MTEDUCARE", "MTNL", "MUKANDENGG", "MUKANDLTD", "MUKANDLTD",
			"MUKTAARTS", "MUNJALAU", "MUNJALSHOW", "MURUDCERA", "MUTHOOTCAP", "MUTHOOTFIN", "MUTHOOTFIN", "MUTHOOTFIN",
			"MVL", "MYSOREBANK", "N100", "NAGAFERT", "NAGAROIL", "NAGREEKCAP", "NAGREEKEXP", "NAHARCAP", "NAHARINDUS",
			"NAHARPOLY", "NAHARSPING", "NAKODA", "NATCOPHARM", "NATHBIOGEN", "NATIONALUM", "NATNLSTEEL", "NAUKRI",
			"NAVINFLUOR", "NAVKARCORP", "NAVNETEDUL", "NBCC", "NBVENTURES", "NCC", "NCLIND", "NDGL", "NDL", "NDTV",
			"NECCLTD", "NECLIFE", "NELCAST", "NELCO", "NEPCMICON", "NESCO", "NESTLEIND", "NETWORK18", "NEULANDLAB",
			"NEXTMEDIA", "NFL", "NH", "NHAI", "NHAI", "NHAI", "NHAI", "NHAI", "NHAI", "NHAI", "NHBTF2014", "NHBTF2014",
			"NHPC", "NHPC", "NIBL", "NICCO", "NIF100IWIN", "NIFTYBEES", "NIFTYEES", "NIFTYIWIN", "NIITLTD", "NIITTECH",
			"NILAINFRA", "NILKAMAL", "NIPPOBATRY", "NITCO", "NITESHEST", "NITINFIRE", "NITINSPIN", "NKIND", "NLCINDIA",
			"NMDC", "NOCIL", "NOIDATOLL", "NRBBEARING", "NSIL", "NTPC", "NTPC", "NTPC", "NTPC", "NTPC", "NTPC",
			"NUCLEUS", "NUTEK", "OBEROIRLTY", "OCCL", "OCL", "OFSS", "OIL", "OILCOUNTUB", "OISL", "OMAXAUTO", "OMAXE",
			"OMKARCHEM", "OMMETALS", "ONELIFECAP", "ONGC", "ONMOBILE", "ONWARDTEC", "OPTOCIRCUI", "ORBITCORP",
			"ORBTEXP", "ORCHIDPHAR", "ORICONENT", "ORIENTABRA", "ORIENTALTL", "ORIENTBANK", "ORIENTBELL", "ORIENTCEM",
			"ORIENTHOT", "ORIENTLTD", "ORIENTPPR", "ORIENTREF", "ORISSAMINE", "ORTEL", "ORTINLABSS", "OUDHSUG", "PAEL",
			"PAGEIND", "PALREDTEC", "PANACEABIO", "PANAMAPET", "PANORAMUNI", "PAPERPROD", "PARABDRUGS", "PARACABLES",
			"PARAGMILK", "PARASPETRO", "PARRYSUGAR", "PARSVNATH", "PATELENG", "PATINTLOG", "PATSPINLTD", "PBAINFRA",
			"PCJEWELLER", "PDMJEPAPER", "PDPL", "PDSMFL", "PDUMJEIND", "PDUMJEPULP", "PEARLPOLY", "PEL", "PENIND",
			"PENINLAND", "PENPEBS", "PERSISTENT", "PETRONENGG", "PETRONET", "PFC", "PFIZER", "PFOCUS", "PFS", "PGEL",
			"PGHH", "PGIL", "PHILIPCARB", "PHOENIXLL", "PHOENIXLTD", "PIDILITIND", "PIIND", "PILANIINVS", "PILITA",
			"PINCON", "PIONDIST", "PIONEEREMB", "PIRPHYTO", "PITTILAM", "PKTEA", "PLASTIBLEN", "PNB", "PNBGILTS", "PNC",
			"PNCINFRA", "PNEUMATIC", "POCHIRAJU", "POKARNA", "POLARIS", "POLYMED", "POLYPLEX", "PONNIERODE",
			"POWERGRID", "POWERMECH", "PPAP", "PRABHAT", "PRAENG", "PRAJIND", "PRAKASH", "PRAKASHCON", "PRAKASHSTL",
			"PRATIBHA", "PRECAM", "PRECOT", "PRECWIRE", "PREMEXPLN", "PREMIER", "PRESSMN", "PRESTIGE", "PRICOL",
			"PRIMESECU", "PRISMCEM", "PROVOGE", "PROZONINTU", "PSB", "PSL", "PSUBNKBEES", "PTC", "PTL", "PUNJABCHEM",
			"PUNJLLOYD", "PURVA", "PVP", "PVR", "QGOLDHALF", "QNIFTY", "QUESS", "QUICKHEAL", "RADAAN", "RADICO", "RAIN",
			"RAINBOWPAP", "RAIREKMOH", "RAJESHEXPO", "RAJOIL", "RAJRAYON", "RAJSREESUG", "RAJTV", "RAJVIR", "RALLIS",
			"RAMANEWS", "RAMASTEEL", "RAMCOCEM", "RAMCOIND", "RAMCOSYS", "RAMKY", "RAMSARUP", "RANASUG", "RANEENGINE",
			"RANEHOLDIN", "RASOYPR", "RATNAMANI", "RAYMOND", "RBL", "RBLBANK", "RCF", "RCOM", "RDEL", "RECLTD",
			"RECLTD", "RECLTD", "RECLTD", "REDINGTON", "REFEX", "REGENCERAM", "REIAGROLTD", "REISIXTEN", "RELAXO",
			"RELBANK", "RELCAPITAL", "RELCNX100", "RELCONS", "RELDIVOPP", "RELGOLD", "RELIANCE", "RELIFIN", "RELIGARE",
			"RELINFRA", "RELNIFTY", "REMSONSIND", "RENUKA", "REPCOHOME", "REPRO", "RESPONIND", "REVATHI", "RICOAUTO",
			"RIIL", "RJL", "RKDL", "RKFORGE", "RMCL", "RML", "RMMIL", "ROHITFERRO", "ROHLTD", "ROLTA", "ROSSELLIND",
			"RPGLIFE", "RPOWER", "RPPINFRA", "RSSOFTWARE", "RSWM", "RSYSTEMS", "RTNINFRA", "RTNPOWER", "RUBYMILLS",
			"RUCHINFRA", "RUCHIRA", "RUCHISOYA", "RUPA", "RUSHIL", "SABTN", "SADBHAV", "SADBHIN", "SAGCEM", "SAIL",
			"SAKHTISUG", "SAKSOFT", "SAKUMA", "SALORAINTL", "SALSTEEL", "SALZERELEC", "SAMBHAAV", "SAMTEL", "SANCO",
			"SANDESH", "SANGAMIND", "SANGHIIND", "SANGHVIFOR", "SANGHVIMOV", "SANOFI", "SANWARIA", "SARDAEN",
			"SAREGAMA", "SARLAPOLY", "SASKEN", "SATHAISPAT", "SATIN", "SBBJ", "SBIN", "SBIN", "SBIN", "SBT",
			"SCHNEIDER", "SCI", "SDBL", "SEAMECLTD", "SEINV", "SELAN", "SELMCL", "SEPOWER", "SEQUENT", "SERVALL",
			"SESHAPAPER", "SETCO", "SETFGOLD", "SETFNIF50", "SETFNIFBK", "SETFNN50", "SEZAL", "SFCL", "SGBAUG24",
			"SGBFEB24", "SGBMAR24", "SGJHL", "SGL", "SHAHALLOYS", "SHAKTIPUMP", "SHALPAINTS", "SHANTIGEAR",
			"SHARDACROP", "SHARDAMOTR", "SHARIABEES", "SHARONBIO", "SHEMAROO", "SHILPAMED", "SHILPI", "SHIRPUR-G",
			"SHIVAMAUTO", "SHIVTEX", "SHK", "SHOPERSTOP", "SHREECEM", "SHREEPUSHK", "SHREERAMA", "SHRENUJ",
			"SHREYANIND", "SHREYAS", "SHRIASTER", "SHRIPISTON", "SHRIRAMCIT", "SHRIRAMCIT", "SHRIRAMCIT", "SHRIRAMEPC",
			"SHYAMCENT", "SHYAMTEL", "SICAGEN", "SICAL", "SIEMENS", "SIGNET", "SIIL", "SIL", "SILINV", "SIMBHALS",
			"SIMPLEX", "SIMPLEXINF", "SINTEX", "SITASHREE", "SITINET", "SIYSIL", "SJVN", "SKFINDIA", "SKIL", "SKIPPER",
			"SKMEGGPROD", "SMARTLINK", "SMLISUZU", "SMPL", "SMSPHARMA", "SNOWMAN", "SOBHA", "SOLARINDS", "SOMANYCERA",
			"SOMATEX", "SOMICONVEY", "SONASTEER", "SONATSOFTW", "SOTL", "SOUTHBANK", "SPAL", "SPARC", "SPECIALITY",
			"SPENTEX", "SPHEREGSL", "SPIC", "SPICEMOBI", "SPLIL", "SPMLINFRA", "SPYL", "SQSBFSI", "SREEL", "SREINFRA",
			"SREINFRA", "SRF", "SRGINFOTEC", "SRHHYPOLTD", "SRIPIPES", "SRSLTD", "SRTRANSFIN", "SRTRANSFIN",
			"SRTRANSFIN", "SRTRANSFIN", "SRTRANSFIN", "SRTRANSFIN", "SRTRANSFIN", "SRTRANSFIN", "SRTRANSFIN",
			"SRTRANSFIN", "SRTRANSFIN", "SSWL", "STAMPEDE", "STAN", "STAR", "STARPAPER", "STCINDIA", "STEL",
			"STERLINBIO", "STERTOOLS", "STOREONE", "STRTECH", "SUBEX", "SUBROS", "SUDAR", "SUDARSCHEM", "SUJANATWR",
			"SUJANAUNI", "SUMEETINDS", "SUMMITSEC", "SUNCLAYLTD", "SUNDARAM", "SUNDARMFIN", "SUNDRMBRAK", "SUNDRMFAST",
			"SUNFLAG", "SUNILHITEC", "SUNPHARMA", "SUNTECK", "SUNTV", "SUPERHOUSE", "SUPERSPIN", "SUPPETRO", "SUPRAJIT",
			"SUPREMEIND", "SUPREMEINF", "SUPREMETEX", "SURANACORP", "SURANAIND", "SURANASOL", "SURANAT&P", "SURYALAXMI",
			"SURYAROSNI", "SUTLEJTEX", "SUVEN", "SUZLON", "SVOGL", "SWANENERGY", "SWARAJENG", "SWELECTES", "SYMPHONY",
			"SYNCOM", "SYNDIBANK", "SYNGENE", "TAINWALCHM", "TAJGVK", "TAKE", "TALBROAUTO", "TALWALKARS", "TANLA",
			"TANTIACONS", "TARAJEWELS", "TARAPUR", "TARMAT", "TATACHEM", "TATACOFFEE", "TATACOMM", "TATAELXSI",
			"TATAGLOBAL", "TATAINVEST", "TATAMETALI", "TATAMOTORS", "TATAMTRDVR", "TATAPOWER", "TATASPONGE",
			"TATASTEEL", "TBZ", "TCFSL", "TCI", "TCIDEVELOP", "TCIFINANCE", "TCS", "TDPOWERSYS", "TEAMLEASE", "TECHIN",
			"TECHM", "TECHNO", "TECHNOFAB", "TEXINFRA", "TEXMOPIPES", "TEXRAIL", "TFCILTD", "TGBHOTELS", "THANGAMAYL",
			"THEMISMED", "THERMAX", "THIRUSUGAR", "THOMASCOOK", "THOMASCOTT", "THYROCARE", "TI", "TIDEWATER", "TIIL",
			"TIJARIA", "TIL", "TIMETECHNO", "TIMKEN", "TINPLATE", "TIPSINDLTD", "TIRUMALCHM", "TITAN", "TNPETRO",
			"TNPL", "TNTELE", "TOKYOPLAST", "TORNTPHARM", "TORNTPOWER", "TPLPLASTEH", "TREEHOUSE", "TRENT", "TRF",
			"TRICOM", "TRIDENT", "TRIGYN", "TRIL", "TRITURBINE", "TRIVENI", "TTKHLTCARE", "TTKPRESTIG", "TTL", "TTML",
			"TUBEINVEST", "TULSI", "TV18BRDCST", "TVSELECT", "TVSMOTOR", "TVSSRICHAK", "TVTODAY", "TWL", "UBHOLDINGS",
			"UBL", "UCALFUEL", "UCOBANK", "UFLEX", "UFO", "UGARSUGAR", "UJAAS", "UJJIVAN", "ULTRACEMCO", "UMANGDAIRY",
			"UMESLTD", "UNICHEMLAB", "UNIENTER", "UNIONBANK", "UNIPLY", "UNITECH", "UNITEDBNK", "UNITEDTEA", "UNITY",
			"UNIVCABLES", "UPERGANGES", "UPL", "USHAMART", "USHERAGRO", "UTIFEFRGR1", "UTINIFTETF", "UTISENSETF",
			"UTTAMSTL", "UTTAMSUGAR", "UVSL", "V2RETAIL", "VADILALIND", "VAIBHAVGBL", "VAKRANGEE", "VALECHAENG",
			"VALUEIND", "VARDHACRLC", "VARDMNPOLY", "VASCONEQ", "VASWANI", "VEDL", "VENKEYS", "VENUSREM", "VESUVIUS",
			"VETO", "VGUARD", "VHL", "VICEROY", "VIDEOIND", "VIDHIDYE", "VIJAYABANK", "VIJIFIN", "VIJSHAN", "VIKASECO",
			"VIMALOIL", "VIMTALABS", "VINATIORGA", "VINDHYATEL", "VINYLINDIA", "VIPIND", "VIPULLTD", "VISAKAIND",
			"VISASTEEL", "VISESHINFO", "VISHNU", "VIVIDHA", "VIVIMEDLAB", "VKSPL", "VLSFINANCE", "VMART", "VOLTAMP",
			"VOLTAS", "VRLLOG", "VSSL", "VSTIND", "VSTTILLERS", "VTL", "WABAG", "WABCOINDIA", "WALCHANNAG", "WANBURY",
			"WEBELSOLAR", "WEIZFOREX", "WEIZMANIND", "WELCORP", "WELENT", "WELINV", "WELSPUNIND", "WENDT", "WHEELS",
			"WHIRLPOOL", "WILLAMAGOR", "WINDMACHIN", "WINSOME", "WIPRO", "WOCKPHARMA", "WONDERLA", "WSI", "WSTCSTPAPR",
			"XCHANGING", "XLENERGY", "XPROINDIA", "YESBANK", "ZANDUREALT", "ZEEL", "ZEEL", "ZEELEARN", "ZEEMEDIA",
			"ZENITHBIR", "ZENSARTECH", "ZENTEC", "ZICOM", "ZODIACLOTH", "ZODJRDMKJ", "ZUARI", "ZUARIGLOB", "ZYDUSWELL",
			"ZYLOG" };

	private static String[] specialStock = new String[] { "ALSTOMT&D", "ASPINWALL", "BAJAJ-AUTO", "BLS", "COX&KINGS",
			"ECLFINANCE", "EHFLNCD", "FOURTHDIM", "GBNOV23", "GIRRESORTS", "GROBTEA", "HCL-INSYS", "HITECH", "HUDCO",
			"IIFLFIN", "IIHFL", "IL&FSENGG", "IL&FSTRANS", "IREDA", "IRFC", "J&KBANK", "L&TFH", "L&TFINANCE",
			"L&TINFRA", "M&M", "M&MFIN", "MBAPL", "MCDOWELL-N", "MITCON", "MOMAI", "MRO-TEK", "NAGAFERT", "NHAI",
			"NHBTF2014", "RELIFIN", "SANCO", "SGBAUG24", "SGBFEB24", "SGBMAR24", "SHIRPUR-G", "SHRIPISTON", "SIIL",
			"STAN", "SURANAT&P", "TCFSL", "UTIFEFRGR1" };

	public static void main(String[] args) throws Exception {

		MagicNumberGenerator magicNumberGenerator = new MagicNumberGenerator();
		long start = System.currentTimeMillis();
		for (String stock : specialStock) {
			try {
				stock = stock.replace("&", "").replaceAll("-", "_");
				String url = "https://www.quandl.com/api/v3/datasets/NSE/" + stock
						+ ".csv?api_key=yhwhU_RHkVxbTtFTff9t&start_date=2013-01-01&end_date=2016-09-30";
				String eodPricesAsString = HttpUtil.getFromUrlAsString(url);
				DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String[] eodPricesAsArray = eodPricesAsString.split("\n");
				LinkedList<QuandlStockPrice> quandlStockPrices = new LinkedList<QuandlStockPrice>();
				for (int i = 1; i < eodPricesAsArray.length; i++) {
					String eodPriceAsString = eodPricesAsArray[i];
					String[] eodPriceAsArray = eodPriceAsString.split(",");
					LocalDate eodDate = LocalDate.parse(eodPriceAsArray[0], dateFormat);
					double open = new Double(eodPriceAsArray[1]);
					double high = new Double(eodPriceAsArray[2]);
					double low = new Double(eodPriceAsArray[3]);
					double last = new Double(eodPriceAsArray[4]);
					double wap = 0d;
					double close = new Double(eodPriceAsArray[5]);
					int tradedQty = new Double(eodPriceAsArray[6]).intValue();
					double turnover = new Double(eodPriceAsArray[7]);

					QuandlStockPrice quandlStockPrice = new QuandlStockPrice(stock, "NSE", "EQ", open, high, low, close,
							last, wap, tradedQty, turnover, eodDate, LocalDateTime.now());
					quandlStockPrices.push(quandlStockPrice);
				}
				MagicNumberData magicNumberData = magicNumberGenerator.generateMagicNumber(10, stock,
						quandlStockPrices);
				System.out.println(magicNumberData);
			} catch (Exception e) {
				logger.info("Error generating Magic number for " + stock);
			}
		}
		System.out.println("Time took for completion " + (System.currentTimeMillis() - start));
	}

	public List<MagicNumberData> generateMagicNumbers(int movingAverage) {
		List<MagicNumberData> magicNumberDatas = new ArrayList<MagicNumberData>();
		Map<String, List<QuandlStockPrice>> stockPriceMap = quandlEODStockPriceRepository.getEODStockPrices();
		for (Stock stockDetailData : stockRepository.getStocks()) {
			if (stockDetailData != null && stockDetailData.getSecurityId() != null) {

				List<QuandlStockPrice> quandlStockPrices = null;
				String securityId = stockDetailData.getSecurityId();
				// List<QuandlStockPrice> quandlStockPrices = stockPriceMap
				// .get(stockDetailData.getSecurityId());

				if (stockPriceMap != null && stockPriceMap.get(stockDetailData.getSecurityId()) != null) {
					quandlStockPrices = stockPriceMap.get(stockDetailData.getSecurityId());
				} else {
					quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(securityId);

				}
				magicNumberDatas
						.add(generateMagicNumber(movingAverage, stockDetailData.getSecurityId(), quandlStockPrices));
			}

			else {
				logger.info("Setting default value for magic number fr stock " + stockDetailData.getSecurityId());
				magicNumberDatas.add(new MagicNumberData(stockDetailData.getSecurityId(), movingAverage));
			}

		}
		magicNumberRepository.updateMagicNumbers(magicNumberDatas);
		return magicNumberDatas;
	}

	public MagicNumberData generateMagicNumber(int movingAverage, String stockCode) {
		MagicNumberData magicNumberData = generateMagicNumberInternal(movingAverage, stockCode);
		List<MagicNumberData> list = new ArrayList<MagicNumberData>();
		list.add(magicNumberData);
		magicNumberRepository.updateMagicNumbers(list);
		return magicNumberData;
	}

	private MagicNumberData generateMagicNumberInternal(int movingAverage, String stockCode) {
		List<QuandlStockPrice> quandlStockPrices = quandlEODStockPriceRepository.getStockPricesFromDB(stockCode);
		logger.info("Retrieved prices for stock :" + quandlStockPrices.size());
		if (quandlStockPrices.size() == 0) {
			logger.info("Setting default value for magic number fr stock " + stockCode);
			return new MagicNumberData(stockCode, movingAverage);
		}
		return generateMagicNumber(movingAverage, stockCode, quandlStockPrices);
	}

	private MagicNumberData generateMagicNumber(int movingAverage, String stockCode,
			List<QuandlStockPrice> quandlStockPrices) {
		MagicNumberData magicNumberData = new MagicNumberData(stockCode, movingAverage);
		Map<LocalDate, Double> priceMap = new HashMap<LocalDate, Double>();
		QuandlStockPrice lastQuandlStockPrice = null;
		try {
			for (QuandlStockPrice quandlStockPrice : quandlStockPrices) {
				priceMap.put(quandlStockPrice.getEodDate(), quandlStockPrice.getClose());
				lastQuandlStockPrice = quandlStockPrice;
			}
			// logger.info("Price Map object is :"+priceMap.toString());
			setMagicNumberForADX(stockCode, magicNumberData, quandlStockPrices, movingAverage, priceMap,
					lastQuandlStockPrice);
			setMagicNumberForBollinger(stockCode, magicNumberData, quandlStockPrices, movingAverage, priceMap,
					lastQuandlStockPrice);
			setMagicNumberForOscillator(stockCode, magicNumberData, quandlStockPrices, movingAverage, priceMap,
					lastQuandlStockPrice);
		} catch (Exception e) {
			logger.info("Error generating Magic number for " + stockCode);
		}
		return magicNumberData;
	}

	private void setMagicNumberForADX(String stockCode, MagicNumberData magicNumberData,
			List<QuandlStockPrice> quandlStockPrices, int movingAverage, Map<LocalDate, Double> priceMap,
			QuandlStockPrice lastQuandlStockPrice) {
		try {
			/*
			 * for(QuandlStockPrice quandlStockPrice : quandlStockPrices){
			 * priceMap.put(quandlStockPrice.getEodDate(),
			 * quandlStockPrice.getClose()); lastQuandlStockPrice =
			 * quandlStockPrice; }
			 */
			Map<Number, Double> maPnlMap = new HashMap<Number, Double>();
			for (int i = 20; i <= 50; i++) {
				SignalComponentHolder signalComponentHolder = new SignalComponentHolder(movingAverage, 3);
				signalComponentHolder.setMagicNumberADX(i);
				List<StockSignalsDTO> stockSignalsDTOs = stockSignalsGenerator.generateSignals(signalComponentHolder,
						quandlStockPrices, "ADX");
				// logger.debug("stock signals list size is:"
				// + stockSignalsDTOs.size());
				List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt = new ArrayList<StockSignalsDTO>();
				StockSignalsDTO lastStockSignalsDTO = null;
				for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOs) {
					if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getAdxSignalPresent())) {
						stockSignalsDTOsWithSignalPresnt.add(stockSignalsDTO);
					}
					lastStockSignalsDTO = stockSignalsDTO;
				}
				// logger.debug("signal present list size is : "
				// + stockSignalsDTOsWithSignalPresnt.size());
				Double pnl = getPnlADX(priceMap, stockSignalsDTOsWithSignalPresnt, lastQuandlStockPrice,
						lastStockSignalsDTO);
				// logger.info("ADX: "+i+" PNL Data:"+pnl);
				maPnlMap.put(i, pnl);
			}
			Entry<Number, Double> maxEntry = getMaxPnl(maPnlMap);
			if (null != maxEntry) {
				magicNumberData.setMagicNumberADX(maxEntry.getKey().intValue());
				magicNumberData.setPnlADX(maxEntry.getValue());
			}
			logger.info("Magic number for " + stockCode + " is " + magicNumberData.getMagicNumberADX() + " with pnl "
					+ magicNumberData.getPnlADX());
		} catch (Exception e) {
			logger.info("Error generating Magic number for " + stockCode);
			e.printStackTrace();
		}

	}

	private void setMagicNumberForBollinger(String stockCode, MagicNumberData magicNumberData,
			List<QuandlStockPrice> quandlStockPrices, int movingAverage, Map<LocalDate, Double> priceMap,
			QuandlStockPrice lastQuandlStockPrice) {
		try {
			Map<Number, Double> maPnlMap = new HashMap<Number, Double>();
			for (Double i = 0.05; i <= .25; i = i + 0.01) {
				SignalComponentHolder signalComponentHolder = new SignalComponentHolder(movingAverage, 3);
				StockSignalsGenerator signalComponentGenerator = new StockSignalsGenerator();
				signalComponentHolder.setMagicNumberBolliger(i);
				List<StockSignalsDTO> stockSignalsDTOs = signalComponentGenerator.generateSignals(signalComponentHolder,
						quandlStockPrices, "BOL");
				// logger.debug("stock signals list size is:"
				// + stockSignalsDTOs.size());
				List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt = new ArrayList<StockSignalsDTO>();
				StockSignalsDTO lastStockSignalsDTO = null;
				for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOs) {
					if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getSignalPresentBollinger())) {
						stockSignalsDTOsWithSignalPresnt.add(stockSignalsDTO);
					}
					lastStockSignalsDTO = stockSignalsDTO;
				}
				// logger.debug("stock signals present list size is:"
				// + stockSignalsDTOsWithSignalPresnt.size());
				Double pnl = getPnlBollinger(priceMap, stockSignalsDTOsWithSignalPresnt, lastQuandlStockPrice,
						lastStockSignalsDTO);
				// logger.info("BOLLINGER: "+i+" PNL Data:"+pnl);
				maPnlMap.put(i, pnl);
			}
			Entry<Number, Double> maxEntry = getMaxPnl(maPnlMap);
			if (null != maxEntry) {
				magicNumberData.setMagicNumberBollinger(maxEntry.getKey().doubleValue());
				magicNumberData.setPnlBollinger(maxEntry.getValue());
			}
			logger.info("Magic number for Bollinger " + stockCode + " is " + magicNumberData.getMagicNumberBollinger()
					+ " with pnl " + magicNumberData.getPnlBollinger());
		} catch (Exception e) {
			logger.info("Error generating Magic number for " + stockCode);
			e.printStackTrace();
		}
	}

	private void setMagicNumberForOscillator(String stockCode, MagicNumberData magicNumberData,
			List<QuandlStockPrice> quandlStockPrices, int movingAverage, Map<LocalDate, Double> priceMap,
			QuandlStockPrice lastQuandlStockPrice) {
		try {
			Map<Number, Double> maPnlMap = new HashMap<Number, Double>();
			for (int i = 5; i <= 25; i++) {
				SignalComponentHolder signalComponentHolder = new SignalComponentHolder(movingAverage, 3);
				StockSignalsGenerator signalComponentGenerator = new StockSignalsGenerator();
				signalComponentHolder.setMagicNumberOscillator(i);
				List<StockSignalsDTO> stockSignalsDTOs = signalComponentGenerator.generateSignals(signalComponentHolder,
						quandlStockPrices, "OSC");
				// logger.debug("stock signals list size is:"
				// + stockSignalsDTOs.size());
				List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt = new ArrayList<StockSignalsDTO>();
				StockSignalsDTO lastStockSignalsDTO = null;
				for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOs) {
					if (IntelliinvestConstants.SIGNAL_PRESENT.equals(stockSignalsDTO.getSignalPresentOscillator())) {
						stockSignalsDTOsWithSignalPresnt.add(stockSignalsDTO);
					}
					lastStockSignalsDTO = stockSignalsDTO;
				}
				// logger.debug("stock signals present list size is:"
				// + stockSignalsDTOsWithSignalPresnt.size());
				Double pnl = getPnlOscillator(priceMap, stockSignalsDTOsWithSignalPresnt, lastQuandlStockPrice,
						lastStockSignalsDTO);
				// logger.info("OSCILLATOR: "+i+" PNL Data:"+pnl);
				maPnlMap.put(i, pnl);
			}
			Entry<Number, Double> maxEntry = getMaxPnl(maPnlMap);
			if (null != maxEntry) {
				magicNumberData.setMagicNumberOscillator(maxEntry.getKey().intValue());
				magicNumberData.setPnlOscillator(maxEntry.getValue());
			}
			logger.info("Magic number for Oscillator " + stockCode + " is " + magicNumberData.getMagicNumberOscillator()
					+ " with pnl " + magicNumberData.getPnlOscillator());
		} catch (Exception e) {
			logger.info("Error generating Magic number for " + stockCode);
			e.printStackTrace();
		}
	}

	private Entry<Number, Double> getMaxPnl(Map<Number, Double> maPnlMap) {
		Entry<Number, Double> maxEntry = null;
		for (Entry<Number, Double> entry : maPnlMap.entrySet()) {
			if (maxEntry == null) {
				maxEntry = entry;
			} else if (entry.getValue() > maxEntry.getValue()) {
				maxEntry = entry;
			}
		}
		return maxEntry;
	}

	public static Double getPnlADX(Map<LocalDate, Double> priceMap,
			List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt, QuandlStockPrice lastQuandlStockPrice,
			StockSignalsDTO lastStockSignalsDTO) {
		Double pnl = 0D;
		StockSignalsDTO stockSignalsDTO_1 = null;
		// logger.info("Signal Present list: "
		// + stockSignalsDTOsWithSignalPresnt.size());
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOsWithSignalPresnt) {
			if (priceMap.get(stockSignalsDTO.getSignalDate()) != null) {
				if (!stockSignalsDTO.getAdxSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)
						&& null == stockSignalsDTO_1) {

				} else if (stockSignalsDTO.getAdxSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl - price;
					// logger.info("pnl in hold case :" +pnl);
				} else if (!stockSignalsDTO.getAdxSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl + price;
					// logger.info("pnl in not BUY case :" +pnl);
				}
			} else {
				logger.info("Price not found for the particular object::::::::::");
			}
			// logger.info("Stock Signals DTO object is:" + stockSignalsDTO);
		}

		if (null != stockSignalsDTO_1
				&& stockSignalsDTO_1.getAdxSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
			Double price = lastQuandlStockPrice.getClose() * lastStockSignalsDTO.getSplitMultiplier();
			pnl = pnl + price;
		}
		return pnl;
	}

	public static Double getPnlBollinger(Map<LocalDate, Double> priceMap,
			List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt, QuandlStockPrice lastQuandlStockPrice,
			StockSignalsDTO lastStockSignalsDTO) {
		Double pnl = 0D;
		StockSignalsDTO stockSignalsDTO_1 = null;
		// logger.info("Signal Present list: "
		// + stockSignalsDTOsWithSignalPresnt.size());
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOsWithSignalPresnt) {
			if (priceMap.get(stockSignalsDTO.getSignalDate()) != null) {
				if (!stockSignalsDTO.getBollingerSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)
						&& null == stockSignalsDTO_1) {

				} else if (stockSignalsDTO.getBollingerSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl - price;
					// logger.info("pnl in BUY case :" +pnl);
				} else if (!stockSignalsDTO.getBollingerSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl + price;
					// logger.info("pnl in not BUY case: "+pnl);
				}
			} else {
				logger.info("Price not found for the particular object::::::::::");
			}
			// logger.info("Stock Signals DTO object is:" + stockSignalsDTO);
		}

		if (null != stockSignalsDTO_1
				&& stockSignalsDTO_1.getBollingerSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
			Double price = lastQuandlStockPrice.getClose() * lastStockSignalsDTO.getSplitMultiplier();
			pnl = pnl + price;
		}
		return pnl;
	}

	public static Double getPnlOscillator(Map<LocalDate, Double> priceMap,
			List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt, QuandlStockPrice lastQuandlStockPrice,
			StockSignalsDTO lastStockSignalsDTO) {
		// logger.info("Signal Present list: "
		// + stockSignalsDTOsWithSignalPresnt.size());
		Double pnl = 0D;
		StockSignalsDTO stockSignalsDTO_1 = null;
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOsWithSignalPresnt) {
			if (priceMap.get(stockSignalsDTO.getSignalDate()) != null) {
				if (!stockSignalsDTO.getOscillatorSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)
						&& null == stockSignalsDTO_1) {
					// logger.info("pnl in first not BUY case :" +pnl);
				} else if (stockSignalsDTO.getOscillatorSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl - price;
					// logger.info("pnl in BUY case :" +pnl);
				} else if (!stockSignalsDTO.getOscillatorSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl + price;
					// logger.info("pnl in not BUY case :" +pnl);
				}
			} else {
				logger.info("Price not found for the particular object::::::::::");
			}
			// logger.info("Stock Signals DTO object is:" + stockSignalsDTO);
		}

		if (null != stockSignalsDTO_1
				&& stockSignalsDTO_1.getOscillatorSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
			Double price = lastQuandlStockPrice.getClose() * lastStockSignalsDTO.getSplitMultiplier();
			pnl = pnl + price;
		}
		return pnl;
	}

	public static Double getPnlMovingAverage(Map<LocalDate, Double> priceMap,
			List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt, QuandlStockPrice lastQuandlStockPrice,
			StockSignalsDTO lastStockSignalsDTO) {
		logger.info("Signal Present list: " + stockSignalsDTOsWithSignalPresnt.size());
		Double pnl = 0D;
		StockSignalsDTO stockSignalsDTO_1 = null;
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOsWithSignalPresnt) {
			if (priceMap.get(stockSignalsDTO.getSignalDate()) != null) {
				if (!stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main()
						.equalsIgnoreCase(IntelliinvestConstants.BUY) && null == stockSignalsDTO_1) {
					// logger.info("pnl in first not BUY case :" +pnl);
				} else if (stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main()
						.equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl - price;
					// logger.info("pnl in BUY case :" +pnl);
				} else if (!stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_Main()
						.equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl + price;
					// logger.info("pnl in not BUY case :" +pnl);
				}
			} else {
				logger.info("Price not found for the particular object::::::::::");
			}
			// logger.info("Stock Signals DTO object is:" + stockSignalsDTO);
		}

		if (null != stockSignalsDTO_1 && stockSignalsDTO_1.getMovingAverageSignals().getMovingAverageSignal_Main()
				.equalsIgnoreCase(IntelliinvestConstants.BUY)) {
			Double price = lastQuandlStockPrice.getClose() * lastStockSignalsDTO.getSplitMultiplier();
			pnl = pnl + price;
		}
		return pnl;
	}

	public static Double getPnlMovingAverageLongTerm(Map<LocalDate, Double> priceMap,
			List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt, QuandlStockPrice lastQuandlStockPrice,
			StockSignalsDTO lastStockSignalsDTO) {
		logger.info("Signal Present list: " + stockSignalsDTOsWithSignalPresnt.size());
		Double pnl = 0D;
		StockSignalsDTO stockSignalsDTO_1 = null;
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOsWithSignalPresnt) {
			if (priceMap.get(stockSignalsDTO.getSignalDate()) != null) {
				if (!stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm()
						.equalsIgnoreCase(IntelliinvestConstants.BUY) && null == stockSignalsDTO_1) {
					// logger.info("pnl in first not BUY case :" +pnl);
				} else if (stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm()
						.equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl - price;
					// logger.info("pnl in BUY case :" +pnl);
				} else if (!stockSignalsDTO.getMovingAverageSignals().getMovingAverageSignal_LongTerm()
						.equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl + price;
					// logger.info("pnl in not BUY case :" +pnl);
				}
			} else {
				logger.info("Price not found for the particular object::::::::::");
			}
			// logger.info("Stock Signals DTO object is:" + stockSignalsDTO);
		}

		if (null != stockSignalsDTO_1 && stockSignalsDTO_1.getMovingAverageSignals().getMovingAverageSignal_LongTerm()
				.equalsIgnoreCase(IntelliinvestConstants.BUY)) {
			Double price = lastQuandlStockPrice.getClose() * lastStockSignalsDTO.getSplitMultiplier();
			pnl = pnl + price;
		}
		return pnl;
	}
	
	public static Double getPnlAgg(Map<LocalDate, Double> priceMap,
			List<StockSignalsDTO> stockSignalsDTOsWithSignalPresnt, QuandlStockPrice lastQuandlStockPrice,
			StockSignalsDTO lastStockSignalsDTO) {
		logger.info("Signal Present list: " + stockSignalsDTOsWithSignalPresnt.size());
		Double pnl = 0D;
		StockSignalsDTO stockSignalsDTO_1 = null;
		for (StockSignalsDTO stockSignalsDTO : stockSignalsDTOsWithSignalPresnt) {
			if (priceMap.get(stockSignalsDTO.getSignalDate()) != null) {
				if (!stockSignalsDTO.getAggSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)
						&& null == stockSignalsDTO_1) {
				} else if (stockSignalsDTO.getAggSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl - price;
					// logger.info("pnl in BUY case :" +pnl);
				} else if (!stockSignalsDTO.getAggSignal().equalsIgnoreCase(IntelliinvestConstants.BUY)) {
					stockSignalsDTO_1 = stockSignalsDTO;
					Double price = priceMap.get(stockSignalsDTO.getSignalDate()) * stockSignalsDTO.getSplitMultiplier();
					pnl = pnl + price;
					// logger.info("pnl in not BUY case :" +pnl);
				}
			} else {
				logger.info("Price not found for the particular object::::::::::");
			}
			// logger.info("Stock Signals DTO object is:" + stockSignalsDTO);
		}

		if (null != stockSignalsDTO_1 && stockSignalsDTO_1.getAggSignal()
				.equalsIgnoreCase(IntelliinvestConstants.BUY)) {
			Double price = lastQuandlStockPrice.getClose() * lastStockSignalsDTO.getSplitMultiplier();
			pnl = pnl + price;
		}
		return pnl;
	}

}
