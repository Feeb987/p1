from flask import Flask, jsonify, send_from_directory, abort
import os

app = Flask(__name__)

@app.route('/')
def home():
    return "Welcome to the Music API!"

# 音樂資料
music_data = {
  "musicMap" : {
      "mus1": [
      {
        "image": "http://10.6.9.8:5001/image/senpai.jpg",
        "mus": "http://10.6.9.8:5001/mp3/senpai.mp3",
        "name":"野獸先輩"
      }
    ],
    "mus2": [
      {
        "image": "http://10.6.9.8:5001/image/gbc.jpg",
        "mus": "http://10.6.9.8:5001/mp3/bgc.mp3",
        "name":"又在冰"
      }
    ],
    "mus3": [
      {
        "image": "http://10.6.9.8:5001/image/raichai.jpg",
        "mus": "http://10.6.9.8:5001/mp3/raichai.mp3",
        "name":"來財"
      }
    ],
    "mus4": [
      {
        "image": "http://10.6.9.8:5001/image/spi.jpg",
        "mus": "http://10.6.9.8:5001/mp3/superIdol.mp3",
        "name":"superIdol"
      }
    ],
    "mus5": [
      {
        "image": "http://10.6.9.8:5001/image/yamada.jpg",
        "mus": "http://10.6.9.8:5001/mp3/「僕は...」.mp3",
        "name":"「僕は...」"
      }
    ],
    "mus6": [
      {
        "image": "http://10.6.9.8:5001/image/夏霞.jpg",
        "mus": "http://10.6.9.8:5001/mp3/夏霞.mp3",
        "name":"夏霞"
      }
    ]
  }
}

# 設定 MP3 存放的資料夾
MUSIC_FOLDER = os.path.join(os.getcwd(), "mp3")
os.makedirs(MUSIC_FOLDER, exist_ok=True)  # 確保資料夾存在

@app.route('/mp3/<filename>')
def download_mp3(filename):
    """ 提供 MP3 檔案下載 """
    file_path = os.path.join(MUSIC_FOLDER, filename)
    if os.path.exists(file_path):
        return send_from_directory(MUSIC_FOLDER, filename, as_attachment=True)
    else:
        return abort(404, "找不到音樂檔案！")

@app.route('/music', methods=['GET'])
def get_music():
    """ 回傳所有音樂資料 """
    return jsonify(music_data)

# 設定圖片存放的資料夾
IMAGE_FOLDER = os.path.join(os.getcwd(), "image")
os.makedirs(IMAGE_FOLDER, exist_ok=True)  # 確保資料夾存在

@app.route('/image/<filename>')
def download_image(filename):
    """ 提供 JPG 檔案下載 """
    file_path = os.path.join(IMAGE_FOLDER, filename)
    if os.path.exists(file_path):
        return send_from_directory(IMAGE_FOLDER, filename, as_attachment=True)
    else:
        return abort(404, "找不到圖片檔案！")

@app.route('/image', methods=['GET'])
def get_images():
    """ 這裡可以回傳圖片列表（如果需要的話）"""
    images = os.listdir(IMAGE_FOLDER)
    return jsonify({"images": images})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)