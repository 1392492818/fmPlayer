//
// Created by fuweicong on 2024/3/3.
//

#include "LoadModule.h"

void LoadModule::Load(string path) {
    Assimp::Importer importer;

    // 通过Assimp加载模型文件
    const aiScene *scene = importer.ReadFile(path, aiProcess_Triangulate | aiProcess_FlipUVs);

    if (!scene || scene->mFlags & AI_SCENE_FLAGS_INCOMPLETE || !scene->mRootNode) {
        LOGE("ERROR::ASSIMP::%s", importer.GetErrorString());
        return;
    }

    // 递归遍历场景图的节点来提取模型数据
    ProcessNode(scene->mRootNode, scene);
    LOGE("测试%s", path.c_str());
}


void LoadModule::ProcessNode(aiNode *node, const aiScene *scene) {
    // 处理当前节点的所有网格
    for (unsigned int i = 0; i < node->mNumMeshes; ++i) {
        aiMesh *mesh = scene->mMeshes[node->mMeshes[i]];
        ProcessMesh(mesh, scene);
    }

    // 递归处理子节点
    for (unsigned int i = 0; i < node->mNumChildren; ++i) {
        ProcessNode(node->mChildren[i], scene);
    }
}

void LoadModule::ProcessMesh(aiMesh *mesh, const aiScene *scene) {
    // 遍历网格的每个顶点
    for (unsigned int i = 0; i < mesh->mNumVertices; ++i) {
        // 提取顶点坐标
        aiVector3D vertex = mesh->mVertices[i];
        // 处理顶点坐标...

        // 如果有法线数据，则提取法线
        if (mesh->HasNormals()) {
            aiVector3D normal = mesh->mNormals[i];
            // 处理法线数据...
        }

        // 如果有纹理坐标，则提取纹理坐标
        if (mesh->HasTextureCoords(0)) {
            aiVector3D texCoord = mesh->mTextureCoords[0][i];
            // 处理纹理坐标...
        }
    }

    // 遍历网格的每个面
    for (unsigned int i = 0; i < mesh->mNumFaces; ++i) {
        aiFace face = mesh->mFaces[i];
        // 处理每个面的索引...
    }
}