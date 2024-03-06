//
// Created by fuweicong on 2024/3/3.
//

#ifndef FMMEDIA_LOADMODULE_H
#define FMMEDIA_LOADMODULE_H

#include "assimp/Importer.hpp"
#include "assimp/scene.h"
#include "assimp/postprocess.h"
#include "Log.h"
#include <iostream>
#include <filesystem>
#include "glm.hpp"
using namespace std;

struct Vertex {
    // position 顶点
    glm::vec3 Position;
    // normal 法向量
    glm::vec3 Normal;
    // texCoords 纹理坐标
    glm::vec2 TexCoords;
    // tangent 切线坐标
    glm::vec3 Tangent;
};


class LoadModule {
public:
    void Load(string path);

    void ProcessNode(aiNode *node, const aiScene *scene);

    void ProcessMesh(aiMesh *mesh, const aiScene *scene);
};


#endif //FMMEDIA_LOADMODULE_H
