# Aric's Blog

## 文档入口
n- [项目文档中心](docs/README.md)
- [项目功能总览](docs/project-overview.md)
- [模块功能说明](docs/modules.md)
- [开发流程规范](docs/standards/development-process.md)

鍩轰簬 **Spring Boot 2.6** 涓?**Vue 3** 鐨勫墠鍚庣鍒嗙鍗氬绯荤粺锛屽墠鍙板弬鑰?Hexo Shoka 涓婚椋庢牸锛屽悗鍙板熀浜庤嫢渚濇敼閫犮€傛敮鎸?**Docker Compose 涓€閿儴缃?*锛屽唴缃涓婚鍒囨崲銆佹晥鐜囧伐鍏枫€丄I 鍔╂墜绛夌壒鑹插姛鑳姐€?

## 椤圭洰缁撴瀯

```
blog/
鈹溾攢鈹€ blog-springboot/          # 鍚庣 Spring Boot 椤圭洰
鈹?  鈹溾攢鈹€ src/main/java/        # Java 婧愮爜
鈹?  鈹溾攢鈹€ src/main/resources/   # 閰嶇疆鏂囦欢
鈹?  鈹斺攢鈹€ pom.xml               # Maven 渚濊禆
鈹溾攢鈹€ blog-vue/
鈹?  鈹溾攢鈹€ shoka-blog/           # 鍓嶅彴鍗氬锛圴ue 3 + Naive UI + Vite 5锛?
鈹?  鈹斺攢鈹€ shoka-admin/          # 鍚庡彴绠＄悊锛圴ue 3 + Element Plus + Vite锛?
鈹溾攢鈹€ deploy/                   # Docker 閮ㄧ讲鐩稿叧
鈹?  鈹溾攢鈹€ docker-compose.yml    # 瀹瑰櫒缂栨帓锛圡ySQL + Redis + 鍚庣 + Nginx锛?
鈹?  鈹溾攢鈹€ Dockerfile            # 鍚庣澶氶樁娈垫瀯寤?
鈹?  鈹溾攢鈹€ Dockerfile.nginx      # 鍓嶇澶氶樁娈垫瀯寤?
鈹?  鈹溾攢鈹€ nginx.conf            # Nginx 閰嶇疆锛堝弽鍚戜唬鐞?+ SSL锛?
鈹?  鈹溾攢鈹€ deploy.sh             # 涓€閿儴缃茶剼鏈紙鍚?Docker 瀹夎锛?
鈹?  鈹溾攢鈹€ update.sh             # 鐑洿鏂拌剼鏈紙鐙珛鏇存柊鍓嶅悗绔級
鈹?  鈹溾攢鈹€ blog.sql              # 鏁版嵁搴撳垵濮嬪寲鑴氭湰
鈹?  鈹斺攢鈹€ .env.example          # 鐜鍙橀噺妯℃澘
鈹斺攢鈹€ README.md
```

## 鎶€鏈爤

| 灞傜骇 | 鎶€鏈?| 鐗堟湰 |
|------|------|------|
| **鍓嶅彴** | Vue 3銆丳inia銆乂ue Router 4銆乀ypeScript銆乂ite 5銆丯aive UI銆丒Charts銆丼wiper | Vue 3.4銆乂ite 5.2 |
| **鍚庡彴绠＄悊** | Vue 3銆丒lement Plus銆乂ite銆乀ypeScript銆丳inia | Vue 3.2銆丒lement Plus 2.3 |
| **鍚庣** | Spring Boot銆丮yBatis-Plus銆丮ySQL銆丷edis銆丼a-Token銆並nife4j (Swagger) | Spring Boot 2.6.14銆丣ava 11 |
| **瀛樺偍** | 闃块噷浜?OSS / 鑵捐 COS / 涓冪墰浜?/ 鏈湴瀛樺偍 | 鍙€?|
| **鎼滅储** | MySQL LIKE / Elasticsearch | ES 7.x 鍙€?|
| **閮ㄧ讲** | Docker Compose銆丯ginx銆佸闃舵鏋勫缓 | 鈥?|
| **AI** | DeepSeek / 鍏煎 OpenAI API 鐨勫ぇ妯″瀷 | 鍙€?|

## 鍔熻兘浠嬬粛

### 鍗氬鍓嶅彴

| 妯″潡 | 鍔熻兘璇存槑 |
|------|---------|
| **棣栭〉** | 鎸夊垎绫诲垎缁勫睍绀烘枃绔狅紙姣忕被鏈€澶?6 绡囷級锛屾墦瀛楁満鏁堟灉锛岃疆鎾浘 |
| **鏂囩珷** | Markdown 娓叉煋銆佷唬鐮侀珮浜€佺洰褰曞鑸€佷笂涓嬬瘒銆侀槄璇婚噺缁熻 |
| **褰掓。** | 鏃堕棿绾垮睍绀哄叏閮ㄦ枃绔?|
| **鍒嗙被 / 鏍囩** | 鍙鍖栧浘琛ㄥ睍绀猴紝鐐瑰嚮璺宠浆瀵瑰簲鏂囩珷鍒楄〃 |
| **鎼滅储** | 鍏抽敭璇嶆悳绱㈡枃绔狅紙鏀寔 MySQL / Elasticsearch锛夛紝鎼滅储鍘嗗彶璁板綍 |
| **璇磋** | 鏈嬪弸鍦堝紡鍔ㄦ€侊紝鏀寔鍥剧墖銆佺偣璧炪€佽瘎璁?|
| **鐩稿唽 / 鍥惧簥** | 鐩稿唽娴忚锛屽浘鐗囦笂浼犵鐞?|
| **鍙嬮摼** | 鍙嬫儏閾炬帴灞曠ず涓庣敵璇?|
| **鐣欒█鏉?* | 寮瑰箷寮忕暀瑷€灞曠ず |
| **璇勮** | 澶氱骇璇勮銆佽〃鎯呭寘銆丂鎻愬強 |
| **鐢ㄦ埛** | 娉ㄥ唽鐧诲綍銆佺涓夋柟鐧诲綍锛圦Q / Gitee / GitHub锛夈€佷釜浜轰腑蹇?|
| **涓婚鍒囨崲** | 涓夊涓婚锛氱櫧澶╋紙Light锛夈€侀粦澶滐紙Dark / 鏋佸厜椋庢牸锛夈€佹笎鍙橈紙Gradient锛夛紝鍚勯〉闈㈡爣棰樿窡闅忎富棰樺彉鑹?|
| **闊充箰鎾斁鍣?* | 鍏ㄥ眬 APlayer锛屾敮鎸佹瓕鍗?|
| **鍏充簬** | 绔欓暱浠嬬粛椤?|

### 鏁堢巼宸ュ叿锛堢櫥褰曞悗鍙敤锛?

| 妯″潡 | 鍔熻兘璇存槑 |
|------|---------|
| **浠ｅ姙鍒楄〃** | 浠诲姟澧炲垹鏀规煡銆佷紭鍏堢骇銆佸垎绫汇€佺姸鎬佸垏鎹€佸垎椤?|
| **浠诲姟姹?* | 鏈垎閰嶄换鍔＄鐞嗭紝鎷栨嫿鍒嗛厤鍒板懆璁″垝 |
| **鏃ュ巻瑙嗗浘** | 鏃ュ巻灞曠ず寰呭姙锛屾寜鏃ユ湡鏌ョ湅鍜岀鐞?|
| **鐢樼壒鍥?* | 浠诲姟鏃堕棿璺ㄥ害鍙鍖?|
| **鏃ヨ** | 姣忔棩鏃ヨ璁板綍锛屾敮鎸佸績鎯呮爣绛?|
| **鎬濊€冩矇娣€** | 鎬濊€冪瑪璁扮鐞嗭紝**鏀寔鍒嗙被绛涢€?*锛屾寜鍒嗙被蹇€熷畾浣?|
| **鏃堕棿绠＄悊** | 鏃堕棿鍧楄褰曪紝甯哥敤浜嬩欢蹇€熷～鍏咃紝鍒嗙被缁熻鍥捐〃 |
| **涔犳儻杩借釜** | 涔犳儻鎵撳崱锛屽彲瑙嗗寲杩借釜 |
| **AI 鍔╂墜** | 鍩轰簬澶фā鍨嬬殑鏅鸿兘鎬荤粨銆佸缓璁€佸璇?|
| **涓婚鑹插垏鎹?* | 绱?/ 钃?/ 缁夸笁濂椾富棰樿壊锛屼竴閿垏鎹?|

### 鍚庡彴绠＄悊

| 妯″潡 | 鍔熻兘璇存槑 |
|------|---------|
| **鏂囩珷绠＄悊** | 鏂板 / 缂栬緫 / 鍒犻櫎 / 缃《 / 鎺ㄨ崘锛孧arkdown 缂栬緫鍣?|
| **鍒嗙被 / 鏍囩** | 澧炲垹鏀规煡 |
| **璇勮 / 鐣欒█** | 瀹℃牳銆佸垹闄ゃ€佸洖澶?|
| **鐢ㄦ埛绠＄悊** | 鐢ㄦ埛鍒楄〃銆佽鑹层€佹潈闄愬垎閰?|
| **鍙嬮摼绠＄悊** | 瀹℃牳鍙嬮摼鐢宠 |
| **璇磋绠＄悊** | 鍙戝竷銆佺紪杈戙€佸垹闄?|
| **鐩稿唽绠＄悊** | 鐩稿唽涓庣収鐗囩鐞?|
| **绔欑偣閰嶇疆** | 绔欑偣鍚嶇О銆佸叕鍛娿€佺ぞ浜ら摼鎺ャ€丼EO 绛?|
| **鏃ュ織** | 鎿嶄綔鏃ュ織銆佺櫥褰曟棩蹇椼€佸畾鏃朵换鍔℃棩蹇?|
| **瀹氭椂浠诲姟** | Quartz 璋冨害锛屾敮鎸?CRON 琛ㄨ揪寮?|

## 鐜瑕佹眰

| 杞欢 | 鐗堟湰瑕佹眰 | 璇存槑 |
|------|---------|------|
| JDK | 11+ | 鍚庣缂栬瘧杩愯 |
| Node.js | 16+锛堟帹鑽?18锛?| 鍓嶇鏋勫缓 |
| MySQL | 8.0+ | 鏁版嵁搴?|
| Redis | 6+ | 缂撳瓨 / 浼氳瘽 |
| Docker | 20.10+ | 閮ㄧ讲锛堝彲閫夛級 |
| Elasticsearch | 7.x | 鎼滅储锛堝彲閫夛紝榛樿鐢?MySQL锛?|

## 鏈湴寮€鍙?

### 1. 鏁版嵁搴撳垵濮嬪寲

```bash
# 鍒涘缓鏁版嵁搴撳苟瀵煎叆鍒濆鏁版嵁
mysql -u root -p -e "CREATE DATABASE blog CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
mysql -u root -p blog < deploy/blog.sql
```

### 2. 鍚姩鍚庣

```bash
cd blog-springboot
# 鏂瑰紡涓€锛氫娇鐢?.env 鏂囦欢锛堟帹鑽愶紝椤圭洰宸查厤缃鍙?.env锛?
cp .env.example .env
# 缂栬緫 .env 濉啓 MySQL銆丷edis 瀵嗙爜绛?
# 鐒跺悗鍚姩
mvn spring-boot:run

# 鏂瑰紡浜岋細鐩存帴淇敼 application-dev.yml
mvn spring-boot:run
```

鍚姩鍚庤闂?API 鏂囨。锛歚http://localhost:9000/doc.html`

### 3. 鍚姩鍓嶅彴

```bash
cd blog-vue/shoka-blog
npm install
npm run dev
```

### 4. 鍚姩鍚庡彴绠＄悊

```bash
cd blog-vue/shoka-admin
npm install
npm run dev
```

### 5. 娉ㄦ剰浜嬮」

- 鏈湴寮€鍙戞椂锛屽墠鍙伴粯璁や唬鐞?API 鍒?`localhost:9000`锛屾棤闇€棰濆閰嶇疆
- 绗笁鏂圭櫥褰曪紙QQ / Gitee / GitHub锛夐渶鍦ㄥ悗鍙扮郴缁熼厤缃腑濉啓 Key/Secret
- 鏂囦欢涓婁紶榛樿涓烘湰鍦板瓨鍌紝濡傞渶 OSS 璇峰湪 `.env` 鎴栧悗鍙伴厤缃腑璁剧疆

## Docker 涓€閿儴缃?

### 鏋舵瀯璇存槑

```
鐢ㄦ埛娴忚鍣?鈫?Nginx(:80/:443)
               鈹溾攢鈹€ /       鈫?鍗氬鍓嶅彴锛堥潤鎬佹枃浠讹級
               鈹溾攢鈹€ /admin  鈫?鍚庡彴绠＄悊锛堥潤鎬佹枃浠讹級
               鈹斺攢鈹€ /api    鈫?Spring Boot(:9000) 鍙嶅悜浠ｇ悊
                                鈹溾攢鈹€ MySQL(:3306)
                                鈹斺攢鈹€ Redis(:6379)
```

鍥涗釜瀹瑰櫒锛歚blog-mysql`銆乣blog-redis`銆乣blog-server`锛堝悗绔級銆乣blog-nginx`锛堝墠绔?+ 鍙嶅悜浠ｇ悊锛?

### 閮ㄧ讲姝ラ

#### 1. 涓婁紶椤圭洰鍒版湇鍔″櫒

```bash
# Git 鍏嬮殕
cd /opt && git clone <浠撳簱鍦板潃> aricOnline

# 鎴栨墦鍖呬笂浼?
tar --exclude='node_modules' --exclude='target' -czf blog.tar.gz blog/
scp blog.tar.gz root@浣犵殑鏈嶅姟鍣?/opt/
ssh root@浣犵殑鏈嶅姟鍣?"cd /opt && tar xzf blog.tar.gz && mv blog aricOnline"
```

#### 2. 閰嶇疆鐜鍙橀噺

```bash
cd /opt/aricOnline/deploy
cp .env.example .env
vim .env
```

**蹇呴』閰嶇疆鐨勯」锛?*

```env
MYSQL_PASSWORD=浣犵殑MySQL瀵嗙爜    # 蹇呮敼
REDIS_PASSWORD=浣犵殑Redis瀵嗙爜    # 蹇呮敼
BLOG_URL=https://浣犵殑鍩熷悕/      # 蹇呮敼
UPLOAD_STRATEGY=local           # local / oss / cos
```

#### 3. 鏀剧疆 SSL 璇佷功锛圚TTPS锛?

```bash
cp /浣犵殑璇佷功璺緞/鍩熷悕.pem  /opt/aricOnline/deploy/
cp /浣犵殑璇佷功璺緞/鍩熷悕.key  /opt/aricOnline/deploy/
```

> 濡備笉闇€瑕?HTTPS锛屼慨鏀?`nginx.conf` 鍘绘帀 SSL 鐩稿叧閰嶇疆鍗冲彲銆?

#### 4. 涓€閿儴缃?

```bash
cd /opt/aricOnline/deploy

# 鏂瑰紡涓€锛氫娇鐢ㄩ儴缃茶剼鏈紙棣栨鎺ㄨ崘锛岃嚜鍔ㄥ畨瑁?Docker锛?
chmod +x deploy.sh
./deploy.sh

# 鏂瑰紡浜岋細宸叉湁 Docker锛岀洿鎺ュ惎鍔?
docker compose up -d --build
```

棣栨鏋勫缓绾?**5-10 鍒嗛挓**锛堜笅杞戒緷璧?+ 缂栬瘧锛夛紝鍚庣画鏈夌紦瀛樹細蹇緢澶氥€?

#### 5. 楠岃瘉

```bash
docker compose ps              # 4 涓鍣ㄩ兘搴斾负 Up
docker compose logs -f         # 鏌ョ湅鍏ㄩ儴鏃ュ織
```

璁块棶鍦板潃锛?
- 鍗氬鍓嶅彴锛歚https://浣犵殑鍩熷悕/`
- 鍚庡彴绠＄悊锛歚https://浣犵殑鍩熷悕/admin`
- API 鏂囨。锛歚https://浣犵殑鍩熷悕/api/doc.html`

## 鏃ュ父鏇存柊

鍒濆鍖栭儴缃插悗锛?*鏃ュ父鏇存柊浠ｇ爜涓嶉渶瑕侀噸鍚暟鎹簱**锛屼娇鐢?`--no-deps` 鐙珛鏇存柊锛?

```bash
cd /opt/aricOnline/deploy

# 浠呮洿鏂板悗绔紙MySQL/Redis 涓嶅彈褰卞搷锛屽仠鏈虹害 30s锛?
git pull && docker compose up -d --build --no-deps blog-server

# 浠呮洿鏂板墠绔紙鍚庣/鏁版嵁搴撲笉鍙楀奖鍝嶏紝鍋滄満绾?10s锛?
git pull && docker compose up -d --build --no-deps nginx

# 鍓嶅悗绔竴璧锋洿鏂帮紙鏁版嵁搴撲笉鍙楀奖鍝嶏級
git pull && docker compose up -d --build --no-deps blog-server nginx
```

涔熷彲浠ヤ娇鐢ㄤ氦浜掑紡鐑洿鏂拌剼鏈細

```bash
chmod +x update.sh
./update.sh          # 浜や簰寮忚彍鍗?
./update.sh backend  # 浠呮洿鏂板悗绔?
./update.sh frontend # 浠呮洿鏂板墠绔?
./update.sh all      # 鍓嶅悗绔竴璧?
```

## 甯哥敤杩愮淮鍛戒护

```bash
# 鐘舵€佹煡鐪?
docker compose ps                    # 瀹瑰櫒鐘舵€?
docker compose logs -f blog-server   # 鍚庣鏃ュ織
docker compose logs -f nginx         # Nginx 鏃ュ織

# 鍚仠
docker compose restart blog-server   # 閲嶅惎鍚庣锛堟敼浜?.env 鍚庣敤锛?
docker compose restart nginx         # 閲嶅惎 Nginx
docker compose down                  # 鍋滄鍏ㄩ儴锛堟暟鎹嵎淇濈暀锛?

# 杩涘叆瀹瑰櫒
docker exec -it blog-mysql mysql -uroot -p   # 杩涘叆 MySQL
docker exec -it blog-redis redis-cli          # 杩涘叆 Redis
docker exec -it blog-server /bin/bash         # 杩涘叆鍚庣

# 娓呯悊
docker image prune -f                # 娓呯悊鏃ч暅鍍忛噴鏀剧鐩?
```

## License

瑙侀」鐩牴鐩綍 `LICENSE` 鏂囦欢銆?
