# CraftGUI Extension

圧縮するアイテムをConfigから設定できるようにしたCraftGUIの改良版

## 動作環境
NMSを使用しているため、Minecraft Ver 1.15.2のみ使用可能です。

※開発中のため、バグがたっくさんあります


## Configの設定
```yaml
# GUIに登録するアイテム一覧
# カラーコードが使用可能です
Items:
  # ページ番号
  page1:
    # アイテムを配置するスロット番号(0～44まで)
    0:
      # 圧縮可能にするかどうか
      enabled: true
      # アイテムID
      material: STONE
      # アイテムの表示名
      displayName: 'スポンジ'
      # 以下のLoresで設定したLore名を設定してください
      lore: 'CommonLore'
      # エンチャントをするかどうか
      enchant: false
      # Model番号を指定する場合に記載
      # デフォルトは0です
      model: 0
      # アイテムの変換に必要なアイテムの設定
      requiredItems:
        # MythicMobsアイテムかどうか
        - isMythic: true
          # 変換に必要なアイテムIDの設定
          # バニラアイテムはtype，MMアイテムはmmidで設定して下さい．
          mmid: '2025_GW_event_items_GWショベル10S'
          # アイテムの表示名
          displayName: '&6GWショベル'
          # アイテムの最小必要数
          amount: 1
        - isMythic: false
          type: DIAMOND_BLOCK
          displayName: '&fダイヤモンドブロック'
          amount: 64
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
  # Loreの設定名
  CommonLore:
    - '&f1行目'
    - '&f2行目'
  need9:
    - '&f圧縮に必要な数: &a9'
```

## 使い方
- `/raggui`：CraftGUI Extensionを開きます

## ライセンス / License
[GNU General Public License v3.0](LICENSE)
