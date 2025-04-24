# CraftGUI Extension

圧縮するアイテムをConfigから設定できるようにしたCraftGUIの改良版

## 動作環境
NMSを使用しているため、Minecraft Ver 1.15.2のみ使用可能です。

※開発中のため、バグがたっくさんあります


## Configの設定
```yaml
# GUIに登録するアイテム一覧
Items:
  # ページ番号
  page1:
    # スロット番号(0～44まで)
    0:
      # 圧縮可能にするかどうか
      # true:有効, false:無効
      enabled: true
      # MythicMobsアイテムかどうか
      # true:MythicMobsアイテム, false:バニラアイテム
      isMythicItem: false
      # アイテムID
      material: 'STONE'
      # アイテムの表示名
      # isMythicItemをtrueにするとdisplayNameが表示名になります
      # isMythicitemがfalseであればmaterialが表示名になります(今後日本語対応可予定)
      displayName: ''
      # 以下のLoresで設定したLore名を記述
      lore: 'CommonLore'
      # エンチャントをするかどうか
      # true:有効, false:無効
      enchant: false
      # Model番号を指定する場合に記載
      # デフォルトは0です
      model: 0
      # 圧縮に必要なアイテム数
      requiredAmount: 64
      # 圧縮後に付与するMythicMobsのアイテムID
      giveMythicItemID: 'isi_ticket'
    1:
      enabled: true
      isMythicItem: true
      material: 'DIAMOND_PICKAXE'
      displayName: '&b&lすごいぴっける'
      lore: 'need9'
      model: 1
      requiredAmount: 9
      giveMythicItemID: custom_pickaxe
  page2:
    0:
      enabled: true
      isMythicItem: true
      material: 'DIAMOND_PICKAXE'
      displayName: '&c&lすごいぴっける2'
      lore: 'need9'
      model: 1
      requiredAmount: 9
      giveMythicItemID: custom_pickaxe

Lores:
  CommonLore:
    - '&f1行目'
    - '&f2行目'
  need9:
    - '&f圧縮に必要な数: &a9'
```

## 使い方
- `/ragui`：GUIを開きます

- `/cge reload`：Configを再読み込みします

## ライセンス / License
[GNU General Public License v3.0](LICENSE)
