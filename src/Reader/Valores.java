package Reader;

import java.util.*;

public class Valores {

    public static final Map<String, List<String>> tabelas = new HashMap<>();

    static {
        tabelas.put("Categoria", Arrays.asList(
                "idCategoria", "Descricao"
        ));
        tabelas.put("Produto", Arrays.asList(
                "idProduto", "Nome", "Descricao", "Preco", "QuantEstoque", "Categoria_idCategoria"
        ));
        tabelas.put("TipoCliente", Arrays.asList(
                "idTipoCliente", "Descricao"
        ));
        tabelas.put("Cliente", Arrays.asList(
                "idCliente", "Nome", "Email", "Nascimento", "Senha", "TipoCliente_idTipoCliente", "DataRegistro"
        ));
        tabelas.put("TipoEndereco", Arrays.asList(
                "idTipoEndereco", "Descricao"
        ));
        tabelas.put("Endereco", Arrays.asList(
                "idEndereco", "EnderecoPadrao", "Logradouro", "Numero", "Complemento", "Bairro", "Cidade", "UF",
                "CEP", "TipoEndereco_idTipoEndereco", "Cliente_idCliente"
        ));
        tabelas.put("Telefone", Arrays.asList(
                "Numero", "Cliente_idCliente"
        ));
        tabelas.put("Status", Arrays.asList(
                "idStatus", "Descricao"
        ));
        tabelas.put("Pedido", Arrays.asList(
                "idPedido", "Status_idStatus", "DataPedido", "ValorTotalPedido", "Cliente_idCliente"
        ));
        tabelas.put("Pedido_has_Produto", Arrays.asList(
                "idPedidoProduto", "Pedido_idPedido", "Produto_idProduto", "Quantidade", "PrecoUnitario"
        ));
    }
}
