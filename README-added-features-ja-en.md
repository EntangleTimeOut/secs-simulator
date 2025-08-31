# 追加機能まとめ / Added Features (Swing SECS Simulator)

日付 / Date: 2025-08-31

---

## 日本語

この派生版に追加・改善した主な機能は以下の通りです。

### 1) Viewer ログのエクスポート機能
- メニュー: File > "Export Viewer Logs..."
- 現在の表示フィルタ（All / Equipment / Host）に一致するログのみをファイルへ書き出します。
- 文字コードは UTF-8 です。
- 既定の保存先フォルダ: JAR を起動した作業ディレクトリ（`user.dir`）。
- 既定のファイル名: `log-YYYYMMDD-HHmmss.txt`（例: `log-20250831-153000.txt`）。

### 2) Viewer の表示と操作性の改善
- Clear ボタン: 画面表示と内部バッファをクリアします。
- フィルタ切替: All / Equipment / Host の 3 種を追加。
- 色分け表示: Equipment=緑、Host=青、その他=標準色。
- SECS-I を含む送受（メッセージ/ブロック）も分類・色分け・フィルタ対象に含めました。
- レイアウト調整: ボタンや切り替えが欠けないように配置を修正。

### 3) 起動オプションの拡張
- `--auto-add <path>`
  - SML ファイルを自動読み込みします。
  - ファイル/ディレクトリ指定に対応。ディレクトリは再帰的に `.sml` を探索します。
  - パースに失敗したファイルはスキップし、処理を継続します。

### 4) 保存ダイアログの既定設定を統一
- Start Logging（"Logging..."）および Export Viewer Logs の保存ダイアログで、
  - 既定のフォルダは JAR 起動ディレクトリ。
  - 既定のファイル名は `log-YYYYMMDD-HHmmss.txt`。

### 使い方メモ
- JAR 起動例（SML を自動追加）:
  - `java -jar SwingSecsSimulator.jar --auto-add C:\path\to\sml`
- Viewer ログのエクスポート:
  - File > Export Viewer Logs... を選び、保存先とファイル名を確認して保存します。
- 注意: "Logging..." は全ログをリアルタイムで記録する機能、Export Viewer Logs は「画面に表示中のフィルタ結果」を即時書き出す補助機能です。

---

## English

The following features and improvements have been added to this Swing-based SECS Simulator variant.

### 1) Export Viewer Logs
- Menu: File > "Export Viewer Logs..."
- Exports only the logs that match the current on-screen filter (All / Equipment / Host).
- Output encoding: UTF-8.
- Default save directory: the process working directory where the JAR was launched (`user.dir`).
- Default filename: `log-YYYYMMDD-HHmmss.txt` (e.g., `log-20250831-153000.txt`).

### 2) Viewer UX improvements
- Clear button: clears both the visible contents and the internal buffer.
- Filter toggles: All / Equipment / Host.
- Color coding: Equipment=green, Host=blue, Others=default.
- SECS-I traffic (messages/blocks) is included in classification, coloring, and filtering.
- Layout adjustments to ensure all controls are visible without clipping.

### 3) Startup option extension
- `--auto-add <path>`
  - Automatically loads SML files.
  - Accepts a file or a directory; directories are scanned recursively for `.sml` files.
  - Files that fail to parse are skipped while continuing with others.

### 4) Unified defaults for save dialogs
- For both Start Logging ("Logging...") and Export Viewer Logs dialogs:
  - Default directory is the JAR launch working directory.
  - Default filename is `log-YYYYMMDD-HHmmss.txt`.

### Quick usage notes
- Launch with auto add (example):
  - `java -jar SwingSecsSimulator.jar --auto-add C:\path\to\sml`
- Export the current viewer logs:
  - Choose File > Export Viewer Logs..., then confirm the destination and filename.
- Note: "Logging..." writes all logs in real time, while "Export Viewer Logs" exports the currently-filtered, on-screen logs on demand.